var Editor = {
  curConfig: {
    dimensions: [740,480]
  },
  canvasArray: new Array()
};

function initCanvas() {
  if(Editor.canvas)
    return;

  createCanvas(1);
}

function createCanvas(no) {
  var oldLength = Editor.canvasArray.length;
  if(oldLength < no){
    for(var i = oldLength; i < no; i++){
        Editor.canvasArray.push(new $.SvgCanvas("#svgcanvasdfa", Editor.curConfig, 'detaut'));
    }
  } else {
    Editor.canvasArray.length = no;
  }

  Editor.canvas = Editor.canvasArray[0];
}

function setNumberOfCanvas(no){
  createCanvas(no)
}

//TODO
function setActualCanvas(no) {
    Editor.canvas = Editor.canvasArray[no];
}

$(document).ready(function() {
  initCanvas();
}); 

