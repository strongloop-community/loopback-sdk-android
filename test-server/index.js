var path = require('path');
var async = require('async');
var loopback = require('loopback');

// start strong-remoting's test server
require('./remoting');

// setup loopback's test server
var app = loopback();
app.dataSource('Memory', {
  connector: loopback.Memory,
  defaultForType: 'db'
});

var lbpn = require('loopback-component-push');
var PushModel = lbpn.createPushModel(app, { dataSource: app.datasources.Memory });
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
var fs = require('fs');
var storage = path.join(__dirname, 'storage');
if (!fs.existsSync(storage))
  fs.mkdirSync(storage);
app.dataSource('storage', {
  connector: require('loopback-component-storage'),
  provider: 'filesystem',
  root: storage
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

app.use(require('morgan')('loopback> :method :url :status'));
app.enableAuth();
app.use(loopback.token({ model: app.models.AccessToken }));
app.use(loopback.rest());
app.listen(3000, function() {
  console.log('LoopBack test server listening on http://localhost:3000/');
});
