Brrr... to much documentation to write...

## Examples

### Web Browser DOM

This adapter handles uses `org.w3c.dom.Node` as bridge type, using this adapter will use the web browser DOM.

- [RuiDOMAdapter](../rui-runtime/src/jsMain/kotlin/hu/simplexion/rui/runtime/dom/RuiDOMAdapter.kt)
- [RuiDOMAdapterFactory](../rui-runtime/src/jsMain/kotlin/hu/simplexion/rui/runtime/dom/RuiDOMAdapterFactory.kt)
- [RuiDOMBridge](../rui-runtime/src/jsMain/kotlin/hu/simplexion/rui/runtime/dom/RuiDOMBridge.kt)
- [RuiDOMPlaceholder](../rui-runtime/src/jsMain/kotlin/hu/simplexion/rui/runtime/dom/RuiDOMPlaceholder.kt)

### Common Test Adapter

This adapter is for testing. It records trace information that describes what happens. To use it
effectively you should enable trace code generation with the `withTrace` option. Can be used on any platform.

Uses [TestNode](../rui-runtime/src/commonMain/kotlin/hu/simplexion/rui/runtime/testing/TestNode.kt) as bridge type.

- [RuiTestAdapter](../rui-runtime/src/commonMain/kotlin/hu/simplexion/rui/runtime/testing/RuiTestAdapter.kt)
- [RuiTestAdapterFactory](../rui-runtime/src/commonMain/kotlin/hu/simplexion/rui/runtime/testing/RuiTestAdapterFactory.kt)
- [RuiTestBridge](../rui-runtime/src/commonMain/kotlin/hu/simplexion/rui/runtime/testing/RuiTestBridge.kt)


