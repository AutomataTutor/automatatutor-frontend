var Editor = {
  curConfigDfaIn: {
    dimensions: [740,480]
  },
  curConfigDfaSol: {
    dimensions: [740,480]
  }
};

function initCanvas() {
  if(!Editor.canvasDfaIn) {
      Editor.canvasDfaIn = new $.SvgCanvas("#svgcanvasdfain", Editor.curConfigDfaIn, 'detaut');
  }
  if(!Editor.canvasDfaSol) {
      Editor.canvasDfaSol = new $.SvgCanvas("#svgcanvasdfasol", Editor.curConfigDfaSol, 'powaut');
  }
}

$(document).ready(function() {
  initCanvas();
});


