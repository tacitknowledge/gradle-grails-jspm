package com.tacitknowledge.gradle.jspm

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.file.FileCollection

/**
 * See complete list of arithmetic features here: https://www.npmjs.com/package/systemjs-builder
 */
class JspmBundle extends JspmTask
{
  final FileCollection inputFiles

  JspmBundle()
  {
    description = 'Bundles all JSPM modules according to configuration'

    inputFiles = project.fileTree("${project.buildDir}/${project.jspm.buildDir}/assets").matching {
      exclude 'bundles/**/*'
      exclude 'jspm-config.js'
    }

    project.afterEvaluate{
      inputs.files inputFiles
      outputs.dir project.file("${project.buildDir}/${project.jspm.buildDir}/assets/bundles")
    }
  }

  @Override
  void exec()
  {
    def configFile = new File("${project.buildDir}/${project.jspm.buildDir}/assets/jspm-config.js")
    configFile.text = configFile.text + '''
      System.config({
        meta: {
          "inline/*": {
            "build": false
          }
        }
      });
    '''

    def hash = getHash()
    project.jspm?.bundles?.each { bundle, definition ->
      println "Doing bundle for: [$definition] - [$bundle]"
      args = ['bundle', definition, "../assets/bundles/${bundle}.${hash}.js", '--inject', '--minify']
      super.exec()
    }

    //minify config
    configFile.text = configFile.text.readLines().collect{ it.trim() }.join('')
  }

  String getHash() {
    def digest = DigestUtils.md5Digest;
    inputFiles.each { digest.update(it.bytes) }
    Hex.encodeHexString(digest.digest())
  }
}
