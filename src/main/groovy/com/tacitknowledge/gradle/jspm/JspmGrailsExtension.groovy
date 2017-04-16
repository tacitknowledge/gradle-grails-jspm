package com.tacitknowledge.gradle.jspm

class JspmGrailsExtension
{
    String version
    String loader = 'none'
    String packageConfigPath = 'grails-app/jspm'
    String buildDir = 'jspmCompile'
    Map<String, String> bundles
    Collection<String> depcache
}
