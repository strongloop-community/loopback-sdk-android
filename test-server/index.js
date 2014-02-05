var loopback = require('loopback');

var app = loopback();
app.dataSource('Memory', {
  connector: loopback.Memory,
  defaultForType: 'db'
});

var lbpn = require('loopback-push-notification');
var PushModel = lbpn(app, { dataSource: app.datasources.Memory });
var Installation = PushModel.Installation;

var Widget = app.model('widget', {
  properties: {
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
  },
  dataSource: 'Memory'
});

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

app.model(loopback.AccessToken);

app.model('User', {
  options: {
    base: 'User',
    relations: {
      accessTokens: {
        model: "AccessToken",
        type: "hasMany",
        foreignKey: "userId"
      }
    }
  },
  dataSource: 'Memory'
});

app.dataSource('mail', { connector: 'mail', defaultForType: 'mail' });
loopback.autoAttach();

app.enableAuth();
app.use(loopback.token({ model: app.models.AccessToken }));
app.use(loopback.rest());
app.listen(3000);
