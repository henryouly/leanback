var BG = chrome.extension.getBackgroundPage();
var tabtitle;//当前标签标题
chrome.windows.getCurrent(function(wnd){
  chrome.tabs.getSelected(wnd.id, function(tab){
    var id="tabid"+tab.id;
    tabtitle=tab.title;
    filltable(BG.mediaurls[id]);
  });
});
function filltable(data) {
  if(data==undefined || data.length==0)
  {
    var id="mediatable";
    document.getElementById(id).className="mylist full";
    return;
  }
  var id1="mediaurl",id2="medialist";
  document.getElementById(id1).innerText+="(共"+data.length+"个)";
  var mytable=document.all(id2);
  mytable.deleteRow();
  for (var i = 0; i < data.length; i++) {
    var index=mytable.rows.length;
    var newrow = mytable.insertRow(index);
    //第一列
    var newcell= newrow.insertCell();
    newcell.innerHTML=(i+1)+".";
    //第二列
    newcell= newrow.insertCell(1);
    var url=data[i];
    url=data[i].url;
    newcell.innerHTML = '<a href="'+url+'" title="'+url+'" target="_blank">' + data[i].name + '</a> <b>(' + data[i].size + ')</b>';
    //第三列
    newcell= newrow.insertCell(2);
    newcell.style.cssText = "text-align:right;";
    newcell.innerHTML = '<input type="button" title="复制网址到剪贴板" value="复制" alt="'+url+'" />';
    newcell.firstChild.addEventListener('click', function(){CopyLink(this.alt);});

    var name=data[i].name;
    var str=name.split(".");
    var ext = str[str.length-1];
    if(["flv","hlv","f4v","mp4"].indexOf(ext)!=-1)
      name=tabtitle+"."+ext;
    newcell= newrow.insertCell(3);
    newcell.style.cssText = "text-align:right;";
    newcell.innerHTML = '<input type="button" title="播放视频 '+name+'" value="播放" alt="'+url+'" />';
    newcell.firstChild.addEventListener('click', function(){sendToServer(this.alt);});
  }
}
function CopyLink(url) {
  var txt =BG.document.createElement("input");
  txt.value=url;
  BG.document.body.appendChild(txt);
  txt.select();
  BG.document.execCommand('Copy'); 
  BG.document.body.removeChild(txt); 
}
function sendToServer(url)
{
  var http = new XMLHttpRequest();
  var server = "https://tvleanback.appspot.com/send";
  var body = "msg=" + url;

  http.open("POST", server, true);
  http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  http.setRequestHeader("Content-length", body.length);
  http.setRequestHeader("Connection", "close");

  http.onreadystatechange = function() {
    if(http.readyState == 4 && http.status == 200) {
      alert(http.responseText);
    }
  }

  http.send(body);
}
