'use strict';
 
const express = require('express');
const https = require('https');
const futures = require('futures');
const whilst = require('async.whilst');
const yelp = require('yelp-fusion');
const client = yelp.client('e7oGK4ikuZqVnZQGRMdJ0980xOFs9tRz57D1ghzKng2MhWfNcwhccS0OsC9Edt_L45RSlNdAAZlxOkoT7cKrs01CUNNx2Jt6HL4dCmLJWNPTDmNihlxQw-kc1ZG2WnYx');
const app = express();
const key = "AIzaSyB4tHdvcCBLAGWr-zaxDJWbfU9rPUUHPj4";

var server = app.listen(8080, function() 
{
	var host = server.address().address;
	var port = server.address().port;
});

app.all('*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
    res.header("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS");
    res.header("X-Powered-By",' 3.2.1')
    res.header("Content-Type", "application/json;charset=utf-8");
    next();
});

app.get('/NS', function(req, res) {
    console.log(req.query);
    if(req.query.radius == '') {
        req.query.radius = '10';
    }
    if(req.query.address != '') {
        var url_geo = "https://maps.googleapis.com/maps/api/geocode/json?address=" + req.query.address + "&key=" + key;
        https.get(url_geo, function(data) {
            var json_geo = '';
            data.on('data', function(d) {
                json_geo += d;
            });
            data.on('end', function() {
                json_geo = JSON.parse(json_geo);
                //console.log(json_geo);
                if(json_geo.results.length != 0) {
                    var location = json_geo.results[0].geometry.location.lat + ',' + json_geo.results[0].geometry.location.lng;
                    var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location + "&radius=" + req.query.radius * 1609 + "&type=" + req.query.type + "&keyword=" + req.query.keyword + "&key=" + key; 
                    NearbySearch(url, res);
                }
            });
        });
    }
    else {
        var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + req.query.location + "&radius=" + req.query.radius * 1609 + "&type=" + req.query.type + "&keyword=" + req.query.keyword + "&key=" + key; 
        NearbySearch(url, res);
    }
});

app.get('/Yelp', function(req, res) {
    console.log(req.query);
    client.businessMatch('best', req.query).then(response => {
        console.log(response.jsonBody.businesses);
        if(response.jsonBody.businesses.length > 0) {
            client.reviews(response.jsonBody.businesses[0].id).then(response => {
                console.log(response.jsonBody.reviews);
                res.send(response.jsonBody.reviews);
            });
        } else {
            res.send(null)
        }
    }).catch(e => {
        console.log(e);
    });
})

function NearbySearch(url, res) {
    //console.log(url);
    var sequence = futures.sequence();
    var res_ns = [];
    sequence.then(function(next) {
        https.get(url, function(data) {
            var json_ns = '';
            data.on('data', function(d) {
                json_ns += d;
            });
            data.on('end',function() {
                console.log(json_ns);
                json_ns = JSON.parse(json_ns);
                if(json_ns.hasOwnProperty('results')) {
                    for(var i = 0; i < json_ns.results.length; i++) {
                        res_ns[i] = new Object();
                        res_ns[i].icon = json_ns.results[i].icon;
                        res_ns[i].placeid = json_ns.results[i].place_id;
                        res_ns[i].name = json_ns.results[i].name;
                        res_ns[i].vicinity = json_ns.results[i].vicinity;
                        res_ns[i].location = json_ns.results[i].geometry.location;  
                    }
                }
                if(!json_ns.hasOwnProperty('next_page_token')) {
                    console.log("123", res_ns);
                    if(json_ns.error_message) {
                        res_ns = "error";
                    }
                    res.send(res_ns);
                }
                next(json_ns);
            });
        }).on('error', function(err) {
            console.log("error");
            res_ns = "error";   
        });
    }).then(function(next, json_ns) {
        whilst(function() {
            return json_ns.hasOwnProperty('next_page_token');
        },function (next) {
            var temp = '';
            var url_next = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=" + json_ns.next_page_token + "&key=" + key;
            https.get(url_next, function(data) {
                data.on('data', function(d) {
                    temp += d;
                });
                data.on('end', function() {
                    console.log(temp);
                    temp = JSON.parse(temp);
                    if(temp.status == "OK") {
                        json_ns = temp;
                        var len = res_ns.length;
                        for(var i = 0; i < json_ns.results.length; i++) {
                            res_ns[i + len] = new Object();
                            res_ns[i + len].icon = json_ns.results[i].icon;
                            res_ns[i + len].placeid = json_ns.results[i].place_id;
                            res_ns[i + len].name = json_ns.results[i].name;
                            res_ns[i + len].vicinity = json_ns.results[i].vicinity;
                            res_ns[i + len].location = json_ns.results[i].geometry.location;  
                        }
                    }
                    if(!json_ns.hasOwnProperty('next_page_token')) {
                        console.log(res_ns);
                        if(json_ns.error_message) {
                            res_ns = "error";
                        }
                        res.send(res_ns);
                    }
                    next();
                });
            });
        },function(err) {
            console.log('ERROR');
            res_ns = "error";
        });
    });
}