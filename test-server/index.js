var path = require('path');
var async = require('async');
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

app.model('Customer', {
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

// storage service

app.dataSource('storage', {
  connector: require('loopback-storage-service'),
  provider: 'filesystem',
  root: path.join(__dirname, 'storage')
});

var Container = app.dataSources.storage.createModel('container');
app.model(Container);

Container.destroyAll = function(cb) {
  Container.getContainers(function(err, containers) {
    if (err) return cb(err);
    async.each(
      containers,
      function(item, next) {
        Container.destroyContainer(item.name, next);
      },
      cb
    );
  });
};

Container.destroyAll.shared = true;
Container.destroyAll.http = { verb: 'del', path: '/' }

app.enableAuth();
app.use(loopback.token({ model: app.models.AccessToken }));
app.use(loopback.rest());
app.listen(3000);
