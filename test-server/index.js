var loopback = require('loopback');

var app = loopback();
var Memory = loopback.createDataSource({
  connector: loopback.Memory
});

var lbpn = require('loopback-push-notification');
var PushModel = lbpn(app, { dataSource: Memory });
var Installation = PushModel.Installation;

var Widget = loopback.createModel('widget', {
  name: {
    type: String,
    required: true
  },
  bars: {
    type: Number,
    required: false
  },
  data: {
    type: Object,
    required: false
  }
});

Widget.attachTo(Memory);
app.model(Widget);

Widget.destroyAll(function () {
  Widget.create({
    name: 'Foo',
    bars: 0,
    data: {
      quux: true
    }
  });
  Widget.create({
    name: 'Bar',
    bars: 1
  });
});

app.use(loopback.rest());
app.listen(3000);
