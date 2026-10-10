"""Compile and execute actual flat-ingredient codec source; no fake game classes."""
from pathlib import Path
import os
import subprocess
import sys

cp=Path('port-diagnostics/classpath.txt')
if not cp.is_file():
    sys.exit('Exact target classpath missing')
classpath=cp.read_text().strip()
out=Path('port-diagnostics/flat-ingredient-smoke')
out.mkdir(parents=True,exist_ok=True)
jdk=Path(os.environ.get('JAVA_HOME',''))
java=str(jdk/'bin/java') if os.environ.get('JAVA_HOME') else 'java'
javac=str(jdk/'bin/javac') if os.environ.get('JAVA_HOME') else 'javac'
sources=[str(Path('src/main/java/net/dries007/tfc/common/recipes/FlatIngredientCodec.java')),
         str(Path('tools/porting/FlatIngredientCodecSmoke.java'))]
subprocess.run([javac,'--release','25','-proc:none','-classpath',classpath,'-d',str(out),*sources],check=True)
subprocess.run([java,'-cp',str(out)+os.pathsep+classpath,'FlatIngredientCodecSmoke'],check=True)
