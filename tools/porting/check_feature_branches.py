"""Read-only Git-history guard for independent, complete-mod feature branches.

This cannot prove compilation, semantic independence, or gameplay correctness.
"""
import argparse
import json
from pathlib import Path
import re
import subprocess


def git(repo, *args):
    result = subprocess.run(
        ['git', '-C', str(repo), *args], text=True,
        stdout=subprocess.PIPE, stderr=subprocess.PIPE,
    )
    if result.returncode:
        raise ValueError(result.stderr.strip() or 'Git operation failed')
    return result.stdout.strip()


def validate_registry(registry):
    if registry.get('schema_version') != 1:
        raise ValueError('Unsupported branch registry schema')
    targets = registry['integration_targets']
    systems = registry['systems']
    if not targets or len(set(targets)) != len(targets):
        raise ValueError('Integration targets must be nonempty and unique')
    names = [system['id'] for system in systems]
    if not names or len(set(names)) != len(names):
        raise ValueError('System IDs must be nonempty and unique')
    if any(not re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', name) for name in names):
        raise ValueError('Invalid system ID')
    if registry['branch_pattern'] != 'feature/{target}/{system}':
        raise ValueError('Unexpected branch pattern')
    for system in systems:
        if system.get('requires_unmerged_features') != []:
            raise ValueError(f"{system['id']} declares an unmerged feature dependency")
    return targets, names


def audit_branch(repo, base_ref, head_ref, sibling_refs):
    """Report stale bases and shared sibling-only ancestry; never mutate refs."""
    base = git(repo, 'rev-parse', '--verify', f'{base_ref}^{{commit}}')
    head = git(repo, 'rev-parse', '--verify', f'{head_ref}^{{commit}}')
    common = git(repo, 'merge-base', base, head)
    errors = []
    if common != base:
        errors.append(f'Refresh from {base_ref} before integration')
    own_commits = set(git(repo, 'rev-list', f'{base}..{head}').splitlines())
    for sibling in sibling_refs:
        sibling_head = git(repo, 'rev-parse', '--verify', f'{sibling}^{{commit}}')
        sibling_only = set(git(repo, 'rev-list', f'{base}..{sibling_head}').splitlines())
        overlap = sorted(own_commits & sibling_only)
        if overlap:
            errors.append(f'Unmerged sibling ancestry from {sibling}: {", ".join(overlap)}')
    return {'branch': head_ref, 'base': base_ref, 'head': head,
            'unique_commits': len(own_commits), 'errors': errors}


def check(repo, registry, remote=None, branch=None):
    targets, systems = validate_registry(registry)
    if git(repo, 'rev-parse', '--is-shallow-repository') == 'true':
        raise ValueError('Fetch full history before checking feature ancestry')
    expected = [(target, f'feature/{target}/{system}')
                for target in targets for system in systems]
    if branch is not None:
        expected = [(target, name) for target, name in expected if name == branch]
        if not expected:
            raise ValueError('Branch must name a registered feature and version target')
    prefix = f'refs/remotes/{remote}/' if remote else 'refs/heads/'
    results = []
    for target, name in expected:
        siblings = [prefix + f'feature/{target}/{system}' for system in systems
                    if name != f'feature/{target}/{system}']
        try:
            results.append(audit_branch(repo, prefix + target, prefix + name, siblings))
        except ValueError as error:
            results.append({'branch': prefix + name, 'errors': [str(error)]})
    return results


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument('--remote', help='Inspect fetched remote-tracking refs, e.g. origin')
    parser.add_argument('--branch', help='Check one registered branch instead of all systems')
    args = parser.parse_args()
    try:
        registry = json.loads((args.repo / 'docs/foundations/FEATURE_BRANCHES.json').read_text())
        results = check(args.repo, registry, args.remote, args.branch)
        failures = [result for result in results if result['errors']]
        print(json.dumps({'checked': len(results), 'failed': len(failures),
                          'failures': failures,
                          'limit': 'Git history only; build and native runtime gates still required'},
                         indent=2))
        return int(bool(failures))
    except (OSError, ValueError, KeyError) as error:
        print(json.dumps({'error': str(error)}))
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
