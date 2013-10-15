## Creating Your Own Model: Subclassing

### Prerequisites

 - **Knowledge of Java and Android App Development**
 - **LoopBack Android SDK** - You should know how to set this up already
    if you've gone through the [Getting Started](#getting-started).
    If not, run through that guide first. It doesn't take long, and it provides
    the basis for this guide.
 - **Schema** - Explaining the type of data to store and why is outside the
    scope of this guide, being tightly coupled to your application's needs.

### Summary

Creating a subclass of Model allows you to profit from all the benefits of
a Java class (e.g. compile-time type checking) within your LoopBack data
types.

### Step 1: Model Class & Properties

As with any Java class, the first step is to build your interface. If we
leave any [custom behaviour](#http://docs.strongloop.com/strong-remoting) for
later, then it's just a few property declarations and we're ready for the
implementation.

```java
import java.math.BigDecimal;
import com.strongloop.android.loopback.Model;

/**
 * A widget for sale.
 */
public class Widget extends Model { // This is a subclass, after all.

  // Being for sale, each widget has a way to be identified and an amount of
  // currency to be exchanged for it. Identifying the currency to be exchanged is
  // left as an uninteresting exercise for any financial programmers reading this.

  private String name;
  private BigDecimal price;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public BigDecimal getPrice() {
    return price;
  }
}
```

### Step 3: Model Repository

The `ModelRepository` is the LoopBack Android SDK's
placeholder for what in Node is a JavaScript prototype representing
a specific "type" of Model on the server. In our example, this would be the
model exposed as "widget" (or similar) on the server:

```javascript
// server-side javascript
var Widget = loopback.createModel('widget', {
  name: String,
  price: Number
});
```

Because of this the className (`'widget'`, above) needs to match the name
that model was given on the server. _If you don't have a model, [see this
guide](#) for more information._ The model _must_ exist (even if the schema is
empty) before it can be interacted with.

**TL;DR** - Use this to make creating Models easier. Match the name or create
your own.

Since `ModelRepository` provides a basic implementation, we only need to
override its constructor to provide the appropriate name.

```java
public class WidgetRepository extends ModelRepository<Widget> {
    public WidgetRepository() {
        super("widget", Widget.class);
    }
}
```

### Step 5: A Little Glue

Just as we did in [the getting started guide](#getting-started), we'll need an
`RestAdapter` instance to connect to our server:

```java
RestAdapter adapter = new RestAdapter("http://myserver:3000");
```

**Remember:** Replace `"http://myserver:3000"` with the complete URL to your
server.

Once we have that adapter, we can create our Repository instance.

```java
WidgetRepository repository = adapter.createRepository(WidgetRepository.class);
```

### Step 6: Profit!

Now that we have a `WidgetRepository` instance, we can:

 - Create a `Widget`

```java
Widget pencil = repository.createModel(ImmutableMap.of("name", "Pencil"));
pencil.price = new BigDecimal("1.50");
```

 - Save said `Widget`

```java
pencil.save(new Model.Callback() {
    @Override
    public void onSuccess() {
        // Pencil now exists on the server!
    }

    @Override
    public void onError(Throwable t) {
        // save failed, handle the error
    }
```

 - Find another `Widget`

```java
repository.findById(2, new ModelRepository.FindCallback<Widget>() {
    @Override
    public void onSuccess(Widget widget) {
        // found!
    }

    public void onError(Throwable t) {
        // handle the error
    }
});
```

 - Remove a `Widget`

```java
pencil.destroy(new Model.Callback() {
    @Override
    public void onSuccess() {
        // No more pencil. Long live Pen!
    }

    @Override
    public void onError(Throwable t) {
        // handle the error
    }
});
```
