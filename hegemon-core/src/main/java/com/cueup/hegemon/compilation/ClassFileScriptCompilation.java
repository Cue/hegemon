package com.cueup.hegemon.compilation;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.optimizer.ClassCompiler;
import org.mozilla.javascript.optimizer.Codegen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Script compilation that just always compiles the script.
 */
public class ClassFileScriptCompilation extends SimpleScriptCompilation {

  private static final Logger LOG = LoggerFactory.getLogger(Script.class);

  private final File root;


  public ClassFileScriptCompilation(File root) {
    super(9);
    this.root = root;
  }


  private File getClassFile(String className) {
    String path = className.replace('.', File.separatorChar) + ".class";
    return new File(this.root, path);
  }


  private void saveClassFiles(Object[] compiled) throws IOException {
    for (int j = 0; j != compiled.length; j += 2) {
      String className = (String) compiled[j];
      byte[] bytes = (byte[]) compiled[j + 1];
      File f = getClassFile(className);
      File dir = f.getParentFile();
      if (dir != null) {
        if (!dir.exists()) {
          dir.mkdirs();
        }
      }

      FileOutputStream os = new FileOutputStream(f);
      try {
        os.write(bytes);
      } finally {
        os.close();
      }
    }
  }

  @Override
  public Script compile(Context c, String name, String source) {
    String digest = Hashing.md5().newHasher().putBytes(source.getBytes()).hash().toString();
    String className = name.replace('/', '.') + "_" + digest;
    File classFile = getClassFile(className);

    if (!classFile.exists()) {
      CompilerEnvirons ce = new CompilerEnvirons();
      ce.initFromContext(c);
      ce.setOptimizationLevel(9);
      ClassCompiler compiler = new ClassCompiler(ce);

      try {
        saveClassFiles(compiler.compileToClassFiles(source, name, 1, className));
      } catch (IOException ex) {
        LOG.error("Error saving class files", ex);
        return super.compile(c, name, source);
      }
    }

    try {
      Object[] nameAndBytes = { className, Files.toByteArray(classFile) };
      return new Codegen().createScriptObject(nameAndBytes, null);
    } catch (IOException ex) {
      LOG.error("Error loading class files", ex);
      return super.compile(c, name, source);
    }
  }

}
