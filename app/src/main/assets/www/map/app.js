


var view = new ol.View({
    center: [0, 0],
    zoom: 2
});
var map = new ol.Map({
    target: 'map',
    renderer: 'canvas',
    view: view,
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        })
    ]
});

