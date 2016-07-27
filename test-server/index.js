// Copyright IBM Corp. 2013,2014. All Rights Reserved.
// Node module: loopback-sdk-android
// This file is licensed under the MIT License.
// License text available at https://opensource.org/licenses/MIT

var path = require('path');
var SG = require('strong-globalize');
SG.SetRootDir(__dirname);
var g = SG();
var async = require('async');
var loopback = require('loopback');

// start strong-remoting's test server
require('./remoting');

// setup loopback's test server
var app = loopback();
app.dataSource('Memory', {
  connector: loopback.Memory,
});

var lbpn = require('loopback-component-push');
var PushModel = lbpn.createPushModel();
app.model(lbpn.Installation, { dataSource: 'Memory' });

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

app.model(loopback.AccessToken, { public: false, dataSource: 'Memory' });
app.model(loopback.ACL, { public: false, dataSource: 'Memory' });
app.model(loopback.Role, { public: false, dataSource: 'Memory' });
app.model(loopback.RoleMapping, { public: false, dataSource: 'Memory' });

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
app.use(loopback.rest());
app.listen(3000, function() {
  console.log(g.f('{{LoopBack}} test server listening on {{http://localhost:3000/}}'));
});
