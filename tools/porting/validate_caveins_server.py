"""Packaged standalone mod integration test. Requires nbtlib; no TFC classes are loaded."""
import json,os,subprocess,threading,time,shutil,io,zlib,struct
from pathlib import Path
import nbtlib,re,argparse
parser=argparse.ArgumentParser(description="Run real packaged-JAR cave-in checks without TFC in a disposable flat world.")
parser.add_argument('--launch',type=Path,required=True,help='exportValidationServerLaunch JSON')
parser.add_argument('--jar',type=Path,required=True)
parser.add_argument('--java',default='java',help='Java 25 executable')
parser.add_argument('--workdir',type=Path,required=True,help='Owned disposable fixture directory; never use a survival world')
args=parser.parse_args()
launch=json.loads(args.launch.read_text())
game=args.workdir.resolve();jar=args.jar.resolve()
if game.exists() and any(game.iterdir()) and not (game/'.caveins-test-world').exists():
 raise SystemExit('Refusing to alter an existing non-fixture directory')
game.mkdir(parents=True,exist_ok=True);(game/'.caveins-test-world').touch()
root=game/'evidence';root.mkdir(exist_ok=True)
(game/'eula.txt').write_text('eula=true\n')
(game/'server.properties').write_text('online-mode=false\nserver-port=0\nserver-ip=127.0.0.1\nlevel-name=world\nlevel-seed=42\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nspawn-protection=0\nview-distance=3\nsimulation-distance=3\nenable-status=false\nmax-tick-time=60000\n')
pack=game/'world/datapacks/fixture'; tags=pack/'data/foundations_caveins/tags/block';tags.mkdir(parents=True,exist_ok=True)
(pack/'pack.mcmeta').write_text(json.dumps({'pack':{'description':'Native cave-ins validation fixture','min_format':[101,1],'max_format':[101,1]}}))
def tag(dirt=False):
 (tags/'collapsible.json').write_text(json.dumps({'replace':False,'values':['minecraft:chest','minecraft:bedrock']+(['minecraft:dirt'] if dirt else [])}))
tag()
classpath=[p for p in launch['classpath'] if not p.replace('\\','/').endswith('/build/classes/java/main') and not p.replace('\\','/').endswith('/build/resources/main')]
assert not any('patchouli' in p.lower() or '/tfc-' in Path(p).name.lower() for p in classpath)
vm=[p for p in launch['jvmArgs'] if not p.startswith('-Dfml.modFolders=')]
vm.insert(0,'-Dfml.modFolders=foundations_caveins%%'+str(jar))
cmd=[args.java,'-Xmx2G',*vm,'-cp',os.pathsep.join(classpath),launch['mainClass'],*launch['args']]
results={'jar':jar.name,'runtime_mods_expected':['minecraft','neoforge','foundations_caveins'],'checks':[]}

def start(label):
 log=(root/('caveins-server-'+label+'.log')).open('w'); lines=[]
 env=os.environ.copy(); env.update(launch['environment'])
 for k in ['JAVA_TOOL_OPTIONS','JAVA_OPTS','GRADLE_OPTS']:env.pop(k,None)
 p=subprocess.Popen(cmd,cwd=game,env=env,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,bufsize=1)
 def pump():
  for line in p.stdout:log.write(line);log.flush();lines.append(line)
  log.close()
 threading.Thread(target=pump,daemon=True).start()
 def wait(mark,timeout=90,start=0):
  until=time.monotonic()+timeout
  while time.monotonic()<until:
   if any(mark in line for line in lines[start:]):return
   if p.poll() is not None:raise RuntimeError('Server exited '+str(p.returncode)+' waiting for '+mark+'\n'+''.join(lines[-20:]))
   time.sleep(.1)
  raise RuntimeError('Timeout waiting for '+mark+'\n'+''.join(lines[-20:]))
 def send(*commands):
  p.stdin.write('\n'.join(commands)+'\n');p.stdin.flush()
 def check(label,conditions):
  mark='CHECK_'+label; a=len(lines)
  for _ in range(60):
   send('execute '+conditions+' run say '+mark)
   time.sleep(.2)
   if any(mark in s for s in lines[a:]):results['checks'].append(label);print(label,flush=True);return
  raise AssertionError(label+'\n'+''.join(lines[-15:]))
 wait('Done (');wait('TFC present: false')
 print('Packaged-JAR server started without TFC: '+label,flush=True)
 return p,lines,wait,send,check

p,lines,wait,send,check=start('first')
try:
 send('forceload add -32 -32 63 63')
 time.sleep(1)
 for x in [0,16,32,48,-16]:
  send(f'fill {x-4} 80 -4 {x+4} 80 4 bedrock',f'fill {x-4} 81 -4 {x+4} 92 4 air')
 send('fill -1 84 -1 1 84 1 stone','fill 15 84 -1 17 84 1 stone','setblock 18 82 0 oak_log','setblock 32 84 0 chest','fill 47 84 -1 49 84 1 dirt','setblock -16 84 0 bedrock','say FIXTURES_READY')
 wait('FIXTURES_READY')
 send('fcaveins trigger 0 83 0','fcaveins trigger 16 83 0','fcaveins trigger 32 83 0','fcaveins trigger 48 83 0','fcaveins trigger -16 83 0')
 conditions=' '.join(f'if block {x} 81 {z} stone' for x in [-1,0,1] for z in [-1,0,1])+' if block 0 84 0 air'
 check('unsupported_roof_falls_and_lands',conditions)
 time.sleep(1)
 check('support_beam_preserves_roof','if block 16 84 0 stone')
 check('block_entity_excluded_even_when_tagged','if block 32 84 0 chest')
 check('immune_block_excluded_even_when_tagged','if block -16 84 0 bedrock')
 check('untagged_material_stays','if block 48 84 0 dirt')
 tag(True);a=len(lines);send('reload');wait('Reloading',start=a);time.sleep(2)
 send('fcaveins trigger 48 83 0')
 check('datapack_reload_enables_material','if block 48 81 0 dirt if block 48 84 0 air')
 a=len(lines);send('fcaveins status','say BEFORE_EXPLOSION');wait('BEFORE_EXPLOSION',start=a)
 before=max(int(n) for line in lines[a:] for n in re.findall(r' fell=(\d+)',line))
 send('fill -21 80 11 -11 80 21 bedrock','fill -21 81 11 -11 94 21 air','fill -19 88 13 -13 88 19 stone','fill -17 84 16 -17 87 16 stone','summon minecraft:tnt -15.5 84 16.5 {fuse:10s}')
 found=False
 for _ in range(40):
  a=len(lines);send('fcaveins status');time.sleep(.25)
  if any(int(n)>before for line in lines[a:] for n in re.findall(r' fell=(\d+)',line)):
   found=True;break
 assert found,'Explosion did not trigger the standalone collapse engine'
 results['checks'].append('explosion_event_triggers_falling_blocks');print('explosion_event_triggers_falling_blocks',flush=True)
 time.sleep(1)
 send('fill -4 80 12 4 80 20 bedrock','fill -4 81 12 4 95 20 air','fill -1 90 15 1 90 17 stone','say PERSISTENCE_FIXTURE_READY')
 wait('PERSISTENCE_FIXTURE_READY')
 send('fcaveins trigger 0 89 16','fcaveins status','save-all flush','stop')
 p.wait(timeout=45)
 assert p.returncode==0
finally:
 if p.poll() is None:p.kill();p.wait()

def block(x,y,z):
 folder=game/'world/dimensions/minecraft/overworld/region'
 region=folder/f'r.{(x>>4)>>5}.{(z>>4)>>5}.mca'
 if not region.exists():region=game/'world/region'/region.name
 data=region.read_bytes();i=((x>>4)&31)+((z>>4)&31)*32;offset=int.from_bytes(data[i*4:i*4+3],'big')*4096
 assert offset
 length=int.from_bytes(data[offset:offset+4],'big');compression=data[offset+4];raw=data[offset+5:offset+4+length]
 assert compression==2
 chunk=nbtlib.File.parse(io.BytesIO(zlib.decompress(raw)))
 section=next(s for s in chunk['sections'] if int(s['Y'])==y>>4)
 states=section['block_states'];palette=states['palette'];index=0
 if len(palette)>1:
  bits=max(4,(len(palette)-1).bit_length());per=64//bits;idx=(y&15)*256+(z&15)*16+(x&15)
  index=(int(states['data'][idx//per])&((1<<64)-1))>>((idx%per)*bits)&((1<<bits)-1)
 return str(palette[index]['Name'])
results['pending_roof_before_restart']=block(0,90,16)
assert results['pending_roof_before_restart']=='minecraft:stone',results
p,lines,wait,send,check=start('restart')
try:
 check('queued_collapse_survives_restart','if block 0 81 16 stone if block 0 90 16 air')
 check('supported_roof_survives_restart','if block 16 84 0 stone')
 send('fcaveins status','save-all flush','stop');p.wait(timeout=45);assert p.returncode==0
finally:
 if p.poll() is None:p.kill();p.wait()
results['final_blocks']={'unsupported_landed':block(0,81,0),'supported_roof':block(16,84,0),'chest':block(32,84,0),'reloaded_material':block(48,81,0),'persisted_landing':block(0,81,16)}
(root/'caveins-native-result.json').write_text(json.dumps(results,indent=2)+'\n')
print(json.dumps(results,indent=2),flush=True)
