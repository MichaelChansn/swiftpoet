/*
 * Copyright 2018 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.swiftpoet.test

import io.outfoxx.swiftpoet.*
import io.outfoxx.swiftpoet.ComposedTypeName.Companion.composed
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint.SAME_TYPE
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter


@DisplayName("(class) TypeSpec Tests")
class ClassSpecTests {

  @Test
  @DisplayName("Generates JavaDoc at before class definition")
  fun testGenJavaDoc() {
    val testClass = TypeSpec.classBuilder("Test")
       .addKdoc("this is a comment\n")
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            /**
             * this is a comment
             */
            class Test {
            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testClass = TypeSpec.classBuilder("Test")
       .addModifiers(Modifier.PUBLIC)
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            public class Test {
            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates type variables")
  fun testGenTypeVars() {
    val testClass = TypeSpec.classBuilder("Test")
       .addTypeVariable(
          TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))
       )
       .addTypeVariable(
          TypeVariableName.typeVariable("Y", TypeVariableName.Bound(composed(".Test3", ".Test4")))
       )
       .addTypeVariable(
          TypeVariableName.typeVariable("Z", TypeVariableName.Bound(SAME_TYPE, ".Test5"))
       )
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            class Test<X, Y, Z> where X : Test2, Y : Test3 & Test4, Z == Test5 {
            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates attributes")
  fun testGenAttributes() {
    val testClass = TypeSpec.classBuilder("Test")
       .addAttribute("available", "iOS 9", "*")
       .addAttribute("dynamicMemberLookup")
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            @available(iOS 9, *)
            @dynamicMemberLookup
            class Test {
            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates type variables (concise)")
  fun testGenTypeVarsConcise() {
    val testClass = TypeSpec.classBuilder("Test")
       .addTypeVariable(
          TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))
       )
       .addTypeVariable(
          TypeVariableName.typeVariable("Z", TypeVariableName.Bound(SAME_TYPE, ".Test5"))
       )
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            class Test<X : Test2, Z == Test5> {
            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates super types")
  fun testGenSuperClass() {
    val testClass = TypeSpec.classBuilder("Test")
       .addSuperType(typeName(".Test2"))
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            class Test : Test2 {
            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates constructor")
  fun testGenConstructor() {
    val testClass = TypeSpec.classBuilder("Test")
       .addFunction(
          FunctionSpec.constructorBuilder()
             .addModifiers(Modifier.REQUIRED)
             .addParameter("value", INT)
             .build()
       )
       .addFunction(
          FunctionSpec.constructorBuilder()
             .addParameter("value", STRING)
             .build()
       )
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            class Test {

              required init(value: Swift.Int) {
              }

              init(value: Swift.String) {
              }

            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates property declarations")
  fun testGenProperties() {
    val testClass = TypeSpec.classBuilder("Test")
       .addProperty("value", INT, Modifier.PRIVATE)
       .addMutableProperty("value2", STRING, Modifier.PUBLIC)
       .addProperty(
          PropertySpec.varBuilder("value3", BOOL, Modifier.INTERNAL)
             .initializer("true")
             .build()
       )
       .addProperty(
          PropertySpec.builder("value4", INT)
             .build()
       )
       .addProperty(
          PropertySpec.builder("valueBy5", INT)
             .getter(FunctionSpec.getterBuilder().addCode("%[return value * 5\n%]").build())
             .setter(FunctionSpec.setterBuilder().addParameter("newVal", INT).addCode("%[value2 = newVal / 5\n%]").build())
             .build()
       )
       .addProperty(
          PropertySpec.builder("valueBy6", INT)
             .getter(FunctionSpec.getterBuilder().addCode("%[return value * 6\n%]").build())
             .setter(FunctionSpec.setterBuilder().addCode("%[value2 = newValue / 6\n%]").build())
             .build()
       )
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            class Test {

              private let value: Swift.Int
              public var value2: Swift.String
              var value3: Swift.Bool = true
              let value4: Swift.Int
              var valueBy5: Swift.Int {
                get {
                  return value * 5
                }
                set(newVal) {
                  value2 = newVal / 5
                }
              }
              var valueBy6: Swift.Int {
                get {
                  return value * 6
                }
                set {
                  value2 = newValue / 6
                }
              }

            }

          """.trimIndent()
       )
    )
  }


  @Test
  @DisplayName("Generates method definitions")
  fun testGenMethods() {
    val testClass = TypeSpec.classBuilder("Test")
       .addFunction(
          FunctionSpec.builder("test1")
             .addCode("")
             .build()
       )
       .addFunction(
          FunctionSpec.builder("test2")
             .addAttribute(AttributeSpec.DISCARDABLE_RESULT)
             .addCode("")
             .build()
       )
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            class Test {

              func test1() {
              }

              @discardableResult
              func test2() {
              }

            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testClassBlder = TypeSpec.classBuilder("Test")
       .addKdoc("this is a comment\n")
       .addAttribute(AttributeSpec.DISCARDABLE_RESULT)
       .addModifiers(Modifier.PUBLIC)
       .addTypeVariable(
          TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))
       )
       .addSuperType(typeName(".Test2"))
       .addFunction(
          FunctionSpec.constructorBuilder()
             .addParameter("value", INT)
             .build()
       )
       .addProperty("value", FLOAT, Modifier.PRIVATE)
       .addProperty("value2", STRING, Modifier.PUBLIC)
       .addFunction(
          FunctionSpec.builder("test1")
             .addCode("")
             .build()
       )
       .build()
       .toBuilder()

    assertThat(testClassBlder.kdoc.formatParts, hasItems("this is a comment\n"))
    assertThat(testClassBlder.attributes, hasItems(AttributeSpec.DISCARDABLE_RESULT))
    assertThat(testClassBlder.kind.modifiers.toImmutableSet(), equalTo(setOf(Modifier.PUBLIC)))
    assertThat(testClassBlder.typeVariables, hasItems(TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))))
    assertThat(testClassBlder.superTypes, hasItems<TypeName>(typeName(".Test2")))
    assertThat(testClassBlder.propertySpecs.map { it.name }, hasItems("value", "value2"))
    assertThat(testClassBlder.functionSpecs.map { it.name }, hasItems("test1"))
  }

}
