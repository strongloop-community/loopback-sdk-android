// Copyright IBM Corp. 2014. All Rights Reserved.
// Node module: loopback-sdk-android
// This file is licensed under the MIT License.
// License text available at https://opensource.org/licenses/MIT

var SG = require('strong-globalize');
var g = SG();
var express = require('express');
var remotes = require('strong-remoting').create();
var SharedClass = require('strong-remoting').SharedClass;

remotes.exports = {
  simple: require('./simple'),
  contract: require('./contract'),
};

remotes.addClass(new SharedClass('SimpleClass', require('./simple-class')));
remotes.addClass(new SharedClass('ContractClass', require('./contract-class')));

var app = express();
app.use(require('morgan')('strong-remoting> :method :url :status'));
app.use(remotes.handler('rest'));

var server = require('http')
  .createServer(app)
  .listen(3001, function() {
    console.log(g.f(
      '{{strong-remoting}} test server listening on {{http://localhost:3001/}}'));
  });
