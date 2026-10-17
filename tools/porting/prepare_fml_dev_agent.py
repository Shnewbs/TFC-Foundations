"""Prepare an opt-in FML development agent for hosts without process command metadata.

FML normally self-attaches this exact bundled class. Passing it with -javaagent
uses FML's first supported instrumentation path, without editing the loader,
changing dependencies, or suppressing any transformations or tests. Generated
JARs and Gradle init scripts must remain local and untracked.
"""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--loader', required=True, type=Path, help='Resolved FancyModLoader loader JAR')
    parser.add_argument('--output', required=True, type=Path, help='Untracked local output directory')
    args = parser.parse_args()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    name = 'net/neoforged/fml/startup/DevAgent.class'
    with zipfile.ZipFile(args.loader) as loader:
        agent_class = loader.read(name)
    agent = output / 'fml-dev-agent.jar'
    with zipfile.ZipFile(agent, 'w', zipfile.ZIP_DEFLATED) as jar:
        jar.writestr('META-INF/MANIFEST.MF',
                     'Manifest-Version: 1.0\n'
                     'Premain-Class: net.neoforged.fml.startup.DevAgent\n'
                     'Agent-Class: net.neoforged.fml.startup.DevAgent\n'
                     'Can-Redefine-Classes: true\n'
                     'Can-Retransform-Classes: true\n\n')
        jar.writestr(name, agent_class)
    with zipfile.ZipFile(agent) as jar:
        assert jar.read(name) == agent_class
    argument = '-javaagent:' + str(agent)
    # JSON quotes form a safe Groovy string for ordinary Windows and POSIX paths.
    # Dollar signs need escaping because double-quoted Groovy strings interpolate.
    quoted = json.dumps(argument).replace('$', '\\$')
    init = output / 'fml-dev-agent.gradle'
    init.write_text('allprojects {\n'
                    '    tasks.withType(Test).configureEach { jvmArgs ' + quoted + ' }\n'
                    '    tasks.withType(JavaExec).configureEach { jvmArgs ' + quoted + ' }\n'
                    '}\n', encoding='utf-8')
    print('Bundled DevAgent class SHA-256:', hashlib.sha256(agent_class).hexdigest())
    print('Local agent prepared. Add -I ' + str(init) + ' to the normal Gradle command.')


if __name__ == '__main__':
    main()
