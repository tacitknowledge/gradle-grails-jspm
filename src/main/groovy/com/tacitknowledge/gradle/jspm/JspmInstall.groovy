package com.tacitknowledge.gradle.jspm

class JspmInstall extends JspmTask
{
  JspmInstall()
  {
    description = "Resolve and download jspm dependencies"

    project.afterEvaluate{
      inputs.file new File("${project.projectDir}/${project.jspm.packageConfigPath}/package.json")
      inputs.file new File("${project.buildDir}/${project.jspm.buildDir}/jspm/package.json")
      outputs.dir new File("${project.buildDir}/${project.jspm.buildDir}/assets/jspm_packages")
    }
  }

  @Override
  void exec()
  {
    args = ['dl-loader', project.jspm.loader, '-y']
    super.exec()

    args = ['install', '-y']
    super.exec()
  }
}
