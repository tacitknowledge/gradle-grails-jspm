package com.tacitknowledge.gradle.jspm

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.jar.JarFile
import java.util.zip.ZipEntry

class MergePackageDefinitionsTask extends DefaultTask
{
  public static final String PACKAGE_JSON_LOCATION = 'META-INF/jspm/package.json'
  final JsonSlurper slurper = new JsonSlurper()

  MergePackageDefinitionsTask()
  {
    group = 'Jspm'
    description = 'Merge package.json definitions from dependencies into one file'
    project.afterEvaluate {
      inputs.file new File("${project.projectDir}/${project.jspm.packageConfigPath}/package.json")
      outputs.file new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json")
    }
  }

  @TaskAction
  void merge()
  {
    def dependenciesFromJars = findDependencyJars().collect{ new JarFile(it) }.collectEntries {
      parse(it, it.getEntry(PACKAGE_JSON_LOCATION)).dependencies
    }
    def myPkgJson = slurper.parse(Paths.get(project.projectDir.path, project.jspm.packageConfigPath, 'package.json').toFile())
    myPkgJson.dependencies = myPkgJson.dependencies ?: [:]
    def tmpDeps = new HashMap(myPkgJson.dependencies)
    myPkgJson.dependencies << dependenciesFromJars
    myPkgJson.dependencies << tmpDeps

    Paths.get(project.buildDir.path, project.jspm.buildDir, 'jspm').toFile().mkdirs()
    Paths.get(project.buildDir.path, project.jspm.buildDir, 'jspm', 'package.json').text = JsonOutput.toJson(myPkgJson)
  }

  //TODO this doesn't sort in dep order
  def findDependencyJars()
  {
    FileCollection runtimeFiles = project.configurations.getByName('runtime') as FileCollection
    FileCollection providedFiles = project.configurations.getByName('provided') as FileCollection
    (runtimeFiles + providedFiles).grep{ !it.exists() || new JarFile(it).getJarEntry(PACKAGE_JSON_LOCATION) }
  }

  def parse(JarFile jar, ZipEntry entry)
  {
    if (entry)
    {
      slurper.parse(jar.getInputStream(entry))
    }
  }
}
