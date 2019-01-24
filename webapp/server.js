const express = require('express');
const path = require('path');
const app = express();
const logger = require('morgan');
const port = 2400;
const hbase = require('hbase');
const { createImageData, Canvas } = require('canvas');

let client = hbase({ host: '10.0.213.28', port: 8080 });

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');


app.get('/', function (req, res) {  //index
  res.render('index', { title: 'BigData Viewer' });
});

app.get('/img', function (req, res) {
  client.table('lgiovannange').row('N42E009.hgt').get('data', function (err, cell) {
    let str = JSON.stringify(cell).substring(1, JSON.stringify(cell).length - 1);
    let obj = JSON.parse(str);
    res.setHeader('Content-Type', 'application/json');
    res.send(obj);
  });
});


app.get('/canvas/:z/:x/:y', function (req, res) {
  let x = req.params.x;
  let y = req.params.y;
  let z = req.params.z;

  console.log('x : ' + x + ', y : ' + y + ', z : ' + z);

  let rowkey = "X" + x + "Y" + y;
  console.log(rowkey);
  client.table('lgiovannange').row(rowkey).get('dem3:heightvalues', function (err, cell) {
    let length = 1201;
    let arraySize = length * length * 4;
    let imgData = createImageData(new Uint8ClampedArray(arraySize), length);
    if (err !== null) {
      let i;
      for (i = 0; i < imgData.data.length; i += 4) {
        imgData.data[i + 0] = 200;
        imgData.data[i + 1] = 255;
        imgData.data[i + 2] = 255;
        imgData.data[i + 3] = 255;
      }

    } else {
      let str = JSON.stringify(cell).substring(1, JSON.stringify(cell).length - 1);
      let obj = JSON.parse(str);
      let heightValues = obj.$.split(', ');
      let i = 0;
      let occur, hv, color;
      heightValues.forEach(element => {
        occur = Number(element.split('x')[0]);
        hv = Number(element.split('x')[1]);
        color = getColor(hv);
        for (let j = 0; j < occur; j++) {
          imgData.data[i + 0] = color.r;
          imgData.data[i + 1] = color.g;
          imgData.data[i + 2] = color.b;
          imgData.data[i + 3] = color.a;
          i += 4;
        }
      });
    }
    let canvas = new Canvas(1201, 1201);
    let ctx = canvas.getContext('2d');
    ctx.putImageData(imgData, 0, 0);
    res.setHeader('Content-Type', 'image/png');
    canvas.pngStream().pipe(res);
  });
});


app.listen(port, function () {
  console.log('Listening on port ' + port);
});

function getColor(hv) {
  let color = {
    r: 0,
    g: 0,
    b: 0,
    a: 0
  };
  if (hv == 0) {
    color.g = 255;
    color.r = 200;
    color.b = 255;
  } else if ((hv > 0) && (hv <= 64)) {
    color.r = hv * 3;
    color.g = 64 + hv * 2;
    color.b = 0;
  } else if (hv > 64 && hv <= 128) {
    color.r = 192 - (hv - 64);
    color.g = 192 - (hv - 64) * 2;
    color.b = 0;
  } else if (hv > 128 && hv <= 239) {
    color.r = hv;
    color.g = 63 + ((hv - 128) * 1.5);
    color.b = 0;
  } else if (hv > 239) {
    color.r = 255;
    color.g = 255;
    color.b = hv;
  }
  color.a = 255;
  return color;
}
