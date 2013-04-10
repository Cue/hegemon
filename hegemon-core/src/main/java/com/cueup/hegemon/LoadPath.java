package com.cueup.hegemon;

/**
 * Interface for a script loader.
 */
public interface LoadPath {

  /**
   * Returns the contents of a file found in this LoadPath.
   * @param name the name of the file to try to locate.
   * @return the String contents of the found file.
   * @throws LoadError if no file is found.
   */
  String load(String name) throws LoadError;

}
