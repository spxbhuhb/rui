Stores provide access to entities through basic CRUD operations.

```kotlin
store += data                        // Create
var data = store.realtime[id]        // Realtime Access - changes on data are distributed real time
data.field = value                   // Realtime Update
var data = store.transactional[id]   // Transactional Access - changes to data are applied and distributed at commit
data.commit()                        // Transactional Update
store.remove(id)                     // Delete
```

Stores at the client side may or may not be connected to a server. When connected they use WebSocket for communication.