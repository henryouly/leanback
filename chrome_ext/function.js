var mediaurls=[];

chrome.webRequest.onResponseStarted.addListener(
    function(data){
      findMedia(data);
    },
    {urls: ["http://*/*", "https://*/*"],types: ["object","other"]},
    ["responseHeaders"]);

const exts=["flv","hlv","f4v","mp4","mp3","wma","swf"];//检测的后缀

function findMedia(data){

  if(data.tabId==-1)//不是标签的请求则返回
    return;

  var size = getHeaderValue("content-length", data);
  if (!size)
    return;

  if (size<102400)//媒体文件最小大小(100KB)
    return;

  var str = data.url.split("?");//url按？分开
  str = str[0].split( "/" );//按/分开
  var name=str[str.length-1].toLowerCase();//得到带后缀的名字
  str=name.split(".");
  var ext = str[str.length-1].toLowerCase();
  var contentType = getHeaderValue("content-type", data).toLowerCase();
  if (contentType && contentType!="application/x-shockwave-flash") 
  {
    var type = contentType.split("/")[0];
    //此处用contentType和文件后缀类型来判断(防止像letv网这样以.letv结尾的后缀,所以此处不单单检查后缀)
    if (type!="video" && type!="audio" && exts.indexOf(ext)== -1)
    {
      var res=testContent(data);//最后再判断下Content-Disposition内容
      if(res==null)//没有附件内容，返回
        return;
      else
        name=res;//得到文件名
    }
  }

  var url = data.url;
  var dealurl=url.replace(/(fs|start|begin)=[0-9]+/g,"").replace(/\?$/,"");//去掉url中开始时间的参数
  var id="tabid"+data.tabId;//记录当前请求所属标签的id
  if(mediaurls[id]==undefined)
    mediaurls[id]=[];
  for (var i = 0; i<mediaurls[id].length; i++) {
    var existUrl=mediaurls[id][i].url.replace(/(fs|start|begin)=[0-9]+/g,"").replace(/\?$/,"");//去掉url中开始时间的参数
    if(existUrl==dealurl)//如果已有相同url则不重复记录
      return;
  }
  var info={name:name,url:url,size:size};
  mediaurls[id].push(info);
  //console.log(id+" "+size+" "+url);
  //console.log(data);
}

function testContent(data)
{
  var str = getHeaderValue('Content-Disposition', data);
  if (!str)
    return null;
  var res = str.match(/^(inline|attachment);\s*filename="?(.*?)"?\s*;?$/i);//匹配attachment;filename=...这样字串
  if (!res)//未能匹配
    return null;

  try{
    var name=decodeURIComponent(res[2]);
    return name;//返回解码后的filename名称
  }
  catch(e) {
  }
  return res[2];//解码失败直接返回编码的名字
}

function getHeaderValue(name, data){
  name = name.toLowerCase();
  for (var i = 0; i<data.responseHeaders.length; i++) {
    if (data.responseHeaders[i].name.toLowerCase() == name) {
      return data.responseHeaders[i].value;
    }
  }
  return null;
}

///标签更新，清除该标签之前记录
chrome.tabs.onUpdated.addListener( function( tabId, changeInfo ){
  if(changeInfo.status=="loading")//在载入之前清除之前记录
{
  var id="tabid"+tabId;//记录当前请求所属标签的id
  if(mediaurls[id])
    mediaurls[id]=[];
}

} );

///标签关闭，清除该标签之前记录
chrome.tabs.onRemoved.addListener( function( tabId ){
  var id="tabid"+tabId;//记录当前请求所属标签的id
  if(mediaurls[id])
    delete mediaurls[id];
} );
