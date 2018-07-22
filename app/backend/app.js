'use strict';
 
const express = require('express');
const https = require('https');
const yelp = require('yelp-fusion');
const moment = require('moment');

const app = express();
const key = "AIzaSyDJ6wevloUGvvQwMF_SKu796XNr2i700No";
const client = yelp.client('e7oGK4ikuZqVnZQGRMdJ0980xOFs9tRz57D1ghzKng2MhWfNcwhccS0OsC9Edt_L45RSlNdAAZlxOkoT7cKrs01CUNNx2Jt6HL4dCmLJWNPTDmNihlxQw-kc1ZG2WnYx');

app.listen(process.env.PORT||3000);
//app.listen(3000);

app.all('*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
    res.header("Access-Control-Allow-Methods","GET");
    res.header("X-Powered-By",' 3.2.1')
    res.header("Content-Type", "application/json;charset=utf-8");
    next();
});

app.get('/NS', function(req, res) {
    if (req.query.radius == '') {
        req.query.radius = '10';
    }
    if (req.query.address) {
        var url_geo = "https://maps.googleapis.com/maps/api/geocode/json?address=" + req.query.address + "&key=" + key;
        https.get(url_geo, function(data) {
            var json = '';
            data.on('data', function(d) {
                json += d;
            });
            data.on('end', function() {
                json = JSON.parse(json);
                if (json.results.length) {
                    var location = json.results[0].geometry.location.lat + ',' + json.results[0].geometry.location.lng;
                    var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location + "&radius=" + req.query.radius * 1609 + "&type=" + req.query.type + "&keyword=" + req.query.keyword + "&key=" + key; 
                    NearbySearch(url, res);
                }
            });
        }).on('error', function(e) {
    	   console.log(e.message); 
        });
    }
    else {
        var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + req.query.location + "&radius=" + req.query.radius * 1609 + "&type=" + req.query.type + "&keyword=" + req.query.keyword + "&key=" + key; 
        NearbySearch(url, res);
    }
});

app.get('/NP', function(req, res) {
    var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=" + req.query.pagetoken + "&key=" + key;
    NearbySearch(url, res);
})

app.get('/PD', function(req, res) {
    var url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + req.query.placeId + "&key=" + key;
    https.get(url, function(data) {
        var json = '';
        data.on('data', function(d) {
            json += d;
        });
        data.on('end', function() {
            json = JSON.parse(json);
            var result = new Object();
            result.name = json.result.name;
            
            result.info = new Object();
            result.info.address = json.result.formatted_address;
            result.info.phone_number = json.result.international_phone_number;
            result.info.price_level = json.result.price_level;
            result.info.rating = json.result.rating;
            result.info.google_page = json.result.url;
            result.info.website = json.result.website;
            
            result.place_id = json.result.place_id;
            
            result.location = json.result.geometry.location;
            
            result.reviews = new Object();
            if(json.result.reviews) {
                result.reviews.google_reviews = [];
                for (var i = 0; i < json.result.reviews.length ; i++) {
                    result.reviews.google_reviews[i] = new Object();
                    result.reviews.google_reviews[i].author_name = json.result.reviews[i].author_name;
                    result.reviews.google_reviews[i].author_url = json.result.reviews[i].author_url;
                    result.reviews.google_reviews[i].profile_photo_url = json.result.reviews[i].profile_photo_url;
                    result.reviews.google_reviews[i].rating = json.result.reviews[i].rating;
                    result.reviews.google_reviews[i].text = json.result.reviews[i].text;
                    result.reviews.google_reviews[i].time = json.result.reviews[i].time;
                }   
            }
            
            var address1 = "";
            if(json.result.adr_address.split('<span class="street-address">')[1]) {
                address1 += json.result.adr_address.split('<span class="street-address">')[1].split('</span>')[0];
            }
            var city = "";
            if(json.result.adr_address.split('<span class="locality">')[1]) {
                city += json.result.adr_address.split('<span class="locality">')[1].split('</span>')[0];
            }
            var state = "";
            if(json.result.adr_address.split('<span class="region">')[1]) {
                state += json.result.adr_address.split('<span class="region">')[1].split('</span>')[0];
            }
            var postal_code = "";
            if(json.result.adr_address.split('<span class="postal-code">')[1]) {
                 postal_code += json.result.adr_address.split('<span class="postal-code">')[1].split('</span>')[0];
            }
            client.businessMatch('best', {
                name: json.result.name,
                city : city,
                state: state,
                country: "US",
                address1: address1,
                postal_code: postal_code
            }).then(response => {
                if (response.jsonBody.businesses.length > 0) {
                    client.reviews(response.jsonBody.businesses[0].id).then(response => {
                        result.reviews.yelp_reviews = [];
                        for (var i = 0; i < response.jsonBody.reviews.length ; i++) {
                            result.reviews.yelp_reviews[i] = new Object();
                            result.reviews.yelp_reviews[i].author_name = response.jsonBody.reviews[i].user.name;
                            result.reviews.yelp_reviews[i].author_url = response.jsonBody.reviews[i].url;
                            result.reviews.yelp_reviews[i].profile_photo_url = response.jsonBody.reviews[i].user.image_url;
                            result.reviews.yelp_reviews[i].rating = response.jsonBody.reviews[i].rating;
                            result.reviews.yelp_reviews[i].text = response.jsonBody.reviews[i].text;
                            result.reviews.yelp_reviews[i].time = moment(response.jsonBody.reviews[i].time_created, 'YYYY-MM-DD HH:mm:ss').unix();
                        }
                        res.send(result);
                    });
                } else {
                    res.send(result);
                }
            }).catch(e => {
                console.log(e);
                res.send(result);
            });
        });
    }).on('error', function(e) {
        console.log(e.message); 
    });
})

app.get('/GP', function(req, res) {
    var url_geo = "https://maps.googleapis.com/maps/api/geocode/json?address=" + req.query.address + "&key=" + key;
        https.get(url_geo, function(data) {
            var json = '';
            data.on('data', function(d) {
                json += d;
            });
            data.on('end', function() {
                json = JSON.parse(json);
                if (json.results.length) {
                    var location = json.results[0].geometry.location.lat + ',' + json.results[0].geometry.location.lng;
                    var url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + location + "&destination=" + req.query.destination + "&mode=" + req.query.mode + "&key=" + key;
                    https.get(url, function(data) {
                        var json = '';
                        data.on('data', function(d) {
                            json += d;
                        });
                        data.on('end', function() {
                            json = JSON.parse(json);
                            if (json.status == "OK") {
                                res.send(json.routes[0].overview_polyline.points);
                                } else {
                                    res.send("No path");
                                }
                            })
                    }).on('error', function(e) {
                        console.log(e.message); 
                    });
                }
            });
        }).on('error', function(e) {
    	   console.log(e.message); 
        });
    
    
})

function NearbySearch(url, res) {
    https.get(url, function(data) {
        var json = '';
        data.on('data', function(d) {
            json += d;
        });
        data.on('end', function() {
            json = JSON.parse(json);
            if (json.status == "OK") {
                var res_ns;
                if (json.results) {
                    res_ns = new Object();
                    res_ns.results = [];
                    for (var i = 0; i < json.results.length; i++) {
                        res_ns.results[i] = new Object();
                        res_ns.results[i].icon = json.results[i].icon;
                        res_ns.results[i].placeid = json.results[i].place_id;
                        res_ns.results[i].name = json.results[i].name;
                        res_ns.results[i].vicinity = json.results[i].vicinity;
                        res_ns.results[i].location = json.results[i].geometry.location;  
                    }
                    if (json.next_page_token) {
                        res_ns.next_page_token = json.next_page_token;
                    }
                }
                if (json.error_message) {
                    res_ns = "ERROR";
                }
                res.send(res_ns);
            } else if (json.status == "INVALID_REQUEST") {
                NearbySearch(url, res);
            } else {
                res.send("No results");
            }
        });
    }).on('error', function(e) {
        console.log(e.message); 
    });
}
