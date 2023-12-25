// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.mpxjs.lang.html.parser;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedAttributeElementType;
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType;
import org.jetbrains.mpxjs.lang.html.VueLanguage;
import org.jetbrains.mpxjs.lang.html.psi.impl.VueRefAttributeImpl;
import org.jetbrains.mpxjs.lang.html.psi.impl.VueRefAttributeStubImpl;

//`VueStubElementTypes` 是一个 Java 接口，它定义了一些常量，这些常量是 Vue 中的元素类型。这些元素类型用于在解析 Vue 文件时标记和处理各种元素。
//
//以下是 `VueStubElementTypes` 中定义的元素类型：
//
//        - `STUBBED_TAG`：用于标记需要进一步解析的 Vue 标签。
//        - `TEMPLATE_TAG`：用于标记 Vue 的 `<template>` 标签。
//        - `STUBBED_ATTRIBUTE`：用于标记需要进一步解析的 Vue 属性。
//        - `SCRIPT_ID_ATTRIBUTE`：用于标记 Vue 的 `script` 标签中的 `id` 属性。
//        - `SRC_ATTRIBUTE`：用于标记 Vue 的 `src` 属性，通常用于 `<script>` 或 `<style>` 标签。
//        - `REF_ATTRIBUTE`：用于标记 Vue 的 `ref` 属性。
//        - `SCRIPT_SETUP_TS_EMBEDDED_CONTENT`：用于标记 Vue 的 `<script setup>` 标签中的 TypeScript 内容。
//        - `SCRIPT_SETUP_JS_EMBEDDED_CONTENT`：用于标记 Vue 的 `<script setup>` 标签中的 JavaScript 内容。
//
//这些元素类型在解析 Vue 文件时非常重要，它们帮助解析器识别和处理 Vue 文件中的各种元素和结构。
public interface VueStubElementTypes {

  int VERSION = 10;

  //`VueStubElementTypes` 中定义的元素类型在解析 Vue 文件时非常重要，它们帮助解析器识别和处理 Vue 文件中的各种元素和结构。以下是这些元素类型在 Vue 代码中的一些示例：
  //
  //        - `STUBBED_TAG`：用于标记需要进一步解析的 Vue 标签。例如，`<my-component></my-component>` 中的 `my-component` 就是一个 `STUBBED_TAG`。
  //
  //        - `TEMPLATE_TAG`：用于标记 Vue 的 `<template>` 标签。例如，`<template><div>Hello</div></template>` 中的 `template` 就是一个 `TEMPLATE_TAG`。
  //
  //        - `STUBBED_ATTRIBUTE`：用于标记需要进一步解析的 Vue 属性。例如，`<div v-if="show">Hello</div>` 中的 `v-if` 就是一个 `STUBBED_ATTRIBUTE`。
  //
  //        - `SCRIPT_ID_ATTRIBUTE`：用于标记 Vue 的 `script` 标签中的 `id` 属性。例如，`<script id="my-script"></script>` 中的 `id` 就是一个 `SCRIPT_ID_ATTRIBUTE`。
  //
  //        - `SRC_ATTRIBUTE`：用于标记 Vue 的 `src` 属性，通常用于 `<script>` 或 `<style>` 标签。例如，`<script src="main.js"></script>` 中的 `src` 就是一个 `SRC_ATTRIBUTE`。
  //
  //        - `REF_ATTRIBUTE`：用于标记 Vue 的 `ref` 属性。例如，`<div ref="myDiv">Hello</div>` 中的 `ref` 就是一个 `REF_ATTRIBUTE`。
  //
  //        - `SCRIPT_SETUP_TS_EMBEDDED_CONTENT`：用于标记 Vue 的 `<script setup>` 标签中的 TypeScript 内容。例如，`<script setup lang="ts">...</script>` 中的 TypeScript 代码就是一个 `SCRIPT_SETUP_TS_EMBEDDED_CONTENT`。
  //
  //        - `SCRIPT_SETUP_JS_EMBEDDED_CONTENT`：用于标记 Vue 的 `<script setup>` 标签中的 JavaScript 内容。例如，`<script setup>...</script>` 中的 JavaScript 代码就是一个 `SCRIPT_SETUP_JS_EMBEDDED_CONTENT`。
  VueStubBasedTagElementType STUBBED_TAG = new VueStubBasedTagElementType("STUBBED_TAG");

  VueTemplateTagElementType TEMPLATE_TAG = new VueTemplateTagElementType();

  XmlStubBasedAttributeElementType STUBBED_ATTRIBUTE =
    new XmlStubBasedAttributeElementType("STUBBED_ATTRIBUTE", VueLanguage.Companion.getINSTANCE());

  VueScriptIdAttributeElementType SCRIPT_ID_ATTRIBUTE = new VueScriptIdAttributeElementType();

  VueSrcAttributeElementType SRC_ATTRIBUTE = new VueSrcAttributeElementType();

  XmlStubBasedElementType<VueRefAttributeStubImpl, VueRefAttributeImpl> REF_ATTRIBUTE = new VueRefAttributeElementType();

  VueScriptSetupEmbeddedContentElementType SCRIPT_SETUP_TS_EMBEDDED_CONTENT =
    new VueScriptSetupEmbeddedContentElementType(JavaScriptSupportLoader.TYPESCRIPT, "SCRIPT_SETUP_TS_");

  VueScriptSetupEmbeddedContentElementType SCRIPT_SETUP_JS_EMBEDDED_CONTENT =
    new VueScriptSetupEmbeddedContentElementType(JavaScriptSupportLoader.ECMA_SCRIPT_6, "SCRIPT_SETUP_JS_");
}
