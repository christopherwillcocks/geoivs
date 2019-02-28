var view, map, vectorLayer, myPosition;
var locating = false;

var styles = {
    'LineString': {
        'surveyPath': [
            new ol.style.Style({
                stroke: new ol.style.Stroke({
                    color: 'red',
                    width: 3
                })
            })
        ]
    },
    'Point': {
        'start': [
            new ol.style.Style({
                image: new ol.style.Circle({
                    radius: 5,
                    fill: new ol.style.Fill({color: 'GREEN'}),
                    stroke: new ol.style.Stroke({color: 'GREEN', width: 1})
                }),
                text: new ol.style.Text({
                    font: '12px Calibri,sans-serif',
                    //offsetX: 20,
                    offsetY: 15,
                    text: "Start",
                    fill: new ol.style.Fill({
                        color: '#000'
                    }),
                    stroke: new ol.style.Stroke({
                        color: '#fff',
                        width: 3
                    })
                })
            })
        ],
        'end': [
            new ol.style.Style({
                image: new ol.style.Circle({
                    radius: 5,
                    fill: new ol.style.Fill({color: 'RED'}),
                    stroke: new ol.style.Stroke({color: 'RED', width: 1})
                }),
                text: new ol.style.Text({
                    font: '12px Calibri,sans-serif',
                    offsetY: 15,
                    text: "End",
                    fill: new ol.style.Fill({
                        color: '#000'
                    }),
                    stroke: new ol.style.Stroke({
                        color: '#fff',
                        width: 3
                    })
                })
            })
        ],
        'note': [
            new ol.style.Style({
                image: new ol.style.Circle({
                    radius: 5,
                    fill: new ol.style.Fill({color: 'ORANGE'}),
                    stroke: new ol.style.Stroke({color: 'ORANGE', width: 1})
                }),
                  text: new ol.style.Text({
                      font: '12px Calibri,sans-serif',
                      offsetY: 15,
                      text: "Note",
                      fill: new ol.style.Fill({
                          color: '#000'
                      }),
                      stroke: new ol.style.Stroke({
                          color: '#fff',
                          width: 3
                      })
                  })
            })
        ],
        'position': function(accuracy){

            return [
                new ol.style.Style({
                    image: new ol.style.Circle({
                        radius: 5,
                        fill: new ol.style.Fill({color: 'BLUE'}),
                        stroke: new ol.style.Stroke({color: 'BLUE', width: 1})
                    }),
                    text: new ol.style.Text({
                        font: '12px Calibri,sans-serif',
                        offsetY: 15,
                        text: "Ø" + accuracy + "m",
                        fill: new ol.style.Fill({
                            color: '#000'
                        }),
                        stroke: new ol.style.Stroke({
                            color: '#fff',
                            width: 3
                        })
                    })
                }),
                new ol.style.Style({
                    image: new ol.style.Circle({
                        radius: accuracy/map.getView().getResolution(),
                        fill: new ol.style.Fill({
                            color: 'rgba(46, 44, 255, 0.2)'
                        }),
                        stroke: new ol.style.Stroke({
                            color: 'BLUE',
                            width: 1
                        })
                    })
                })
            ]
        }
    }
};

function panto (x,y){
    var view = map.getView();
    var point = ol.proj.transform([x, y], 'EPSG:4326', 'EPSG:3857');
    var pan = ol.animation.pan({
            duration: 700,
            source: (view.getCenter())
        });
    map.beforeRender(pan);
    view.setCenter(point);
}

function locateMe(){
    document.getElementById('viewfinder').style.opacity = 0;
    var pan = ol.animation.pan({
        duration: 700,
        source: (view.getCenter())
    });
    var zoom = ol.animation.zoom({
        duration: 700,
        resolution: map.getView().getResolution()
    });
    map.beforeRender(pan,zoom);
    view.setCenter(myPosition.getGeometry().getCoordinates());
    view.setZoom(17);
}

function addPoint(coord, name){
    var f = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.transform(coord, 'EPSG:4326', 'EPSG:3857')),
        name: name,
        accuracy: 100
    });
    f.setId(name);
    vectorLayer.getSource().addFeature(f);
}

function addLine(arrayOfCoordinates, name){
    //console.log("Adding points: " + arrayOfCoordinates);
    var line = [];
    for(var c = 0; c < arrayOfCoordinates.length; c++){
        //console.log(" > " + arrayOfCoordinates[c]);
        line.push(ol.proj.transform(arrayOfCoordinates[c], 'EPSG:4326', 'EPSG:3857'));
    }
    var f = new ol.Feature({
        geometry: new ol.geom.LineString(line),
        name: name,
        accuracy: 100
    });
    f.setId(name);
    vectorLayer.getSource().addFeature(f);
}

function addPointToLine(coord, lineStringName){
    var geom = vectorLayer.getSource().getFeatureById(lineStringName).getGeometry();
    var coordinates = geom.getCoordinates();
    coordinates.push(ol.proj.transform(coord, 'EPSG:4326', 'EPSG:3857'));
    geom.setCoordinates(coordinates);
}

function removePoint(name){
    vectorLayer.getSource().removeFeature(vectorLayer.getSource().getFeatureById(name))
}

function zoomToExtent(){
    locating = false;
    document.getElementById('viewfinder').style.opacity = 0;
    var zoom = view.getZoom();
    view.fitExtent(
        vectorLayer.getSource().getExtent(),
        map.getSize()
    );

    var zoom = ol.animation.zoom({
        duration: 250,
        resolution: map.getView().getResolution()
    });
    map.beforeRender(zoom);

    if(zoom>view.getZoom()-1){
        view.setZoom(view.getZoom()-1);
    }else{
        view.setZoom(view.getZoom()-1);
    }
}

function zoomToBox(minx, miny, maxx, maxy){
    document.getElementById('viewfinder').style.opacity = 0;
    var min = ol.proj.transform([minx,miny], 'EPSG:4326', 'EPSG:3857');
    var max = ol.proj.transform([maxx,maxy], 'EPSG:4326', 'EPSG:3857');
    //console.log("zoomToExtent: " + minx + ", " + min);
    map.getView().fitExtent(
        [max[0], max[1], min[0], min[1]],
        map.getSize()
    );
}

/**
    conf: {
        center: [lon(float),lat(float)],
        zoom: zoomLevel(int)
    }
*/

function initMap(conf){

    console.log("Conf: " + JSON.stringify(conf));

    var center = false;
    if (conf.center){
        center = ol.proj.transform(conf.center, 'EPSG:4326', 'EPSG:3857');
    }

    view = new ol.View({
        center: center?center:[0, 0],
        zoom: conf.zoom?conf.zoom:2,
        maxZoom: 18
    });

    var image = new ol.style.Circle({
        radius: 5,
        fill: new ol.style.Fill({color: 'red'}),
        stroke: new ol.style.Stroke({color: 'red', width: 1})
    });

    vectorLayer = new ol.layer.Vector({
        source: new ol.source.GeoJSON(),
        style: function(feature, resolution) {
            if(feature.getProperties().name=='position'){
                return styles[feature.getGeometry().getType()][feature.getProperties().name](feature.getProperties().accuracy);
            }else{
                return styles[feature.getGeometry().getType()][feature.getProperties().name];
            }
        }
    });

    var layers = [
        new ol.layer.Tile({
            style: 'Road',
            source: new ol.source.MapQuest({layer: 'osm'})
        }),
        vectorLayer
    ];

    if (conf.geolocation){
        //console.log("Enabling geolocation");
        myPosition = new ol.Feature({name: 'position', accuracy: 100});
        vectorLayer.getSource().addFeature(myPosition);
        locating = true;
    }

    map = new ol.Map({
        target: 'map',
        renderer: 'canvas',
        view: view,
        layers: layers,
        controls: ol.control.defaults({
            attribution: false,
            rotate: false,
            zoom: false
        })
    });

    map.on('moveend', function (evt) {
       var center = ol.proj.transform(view.getCenter(), 'EPSG:3857', 'EPSG:4326');
       if(window.Camre){
           window.Camre.updateCenter(center.toString());
       }
    });

    map.getViewport().ontouchstart = function(){
        //console.log("ontouchstart");
        document.getElementById('viewfinder').style.opacity = 1;
        locating = false;
        if(window.Camre){
           window.Camre.stopFollowing();
       }
    }

}