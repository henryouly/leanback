import cgi
import json
import sys
import urllib2
import webapp2

reg_id_set = set()

class Main(webapp2.RequestHandler):
  def get(self):
    """
       Show all the registered devices, with a form the send them a
    message.
    """
    global reg_id_set

    reg_id_list = list(reg_id_set)

    html = """
<html>
  <head>
    <title>Sample GCM Server</title>
  </head>
<body>
"""

    if len(reg_id_list) == 0:
      html += "<h3>No registered devices</h3>"
    else:
      html += """<h3>Registered Ids</h3>
      <form
        action="/send"
        method="post">
        Message: <input name="msg" size="30" />
        <input type="submit" value="Send" />
        <br />
        Devices: <table>"""

      for reg_id in reg_id_list:
        html += """
          <tr>
            <td>
              <input type="checkbox" name="reg_id" value="%s" checked />%s
            </td>
          </tr>
        """ % (reg_id, reg_id)

      html += """
      </table>
    </form>"""

    html += """
  </body>
</html>
"""
    self.response.set_status(200)
    self.response.headers['Content-Type'] = 'text/html'
    self.response.out.write(html)


class RegisterHandler(webapp2.RequestHandler):
  """
    Stores the registration ids sent via the 'reg_id' parameter

    Sample request:
    curl http://localhost:8080/register?reg_id=test_id
  """
  def get(self):
    global reg_id_set
    if 'reg_id' in self.request.GET and len(self.request.GET['reg_id']) > 0:
      reg_id_set.add(self.request.GET['reg_id'])
      self.response.set_status(200)
      return
    self.response.set_status(400)

  def post(self):
    """
      Stores the registration ids sent via the 'reg_id' parameter

      Sample request:
      curl -d "reg_id=test_id" http://localhost:8080/register
    """
    global reg_id_set
    if 'reg_id' in self.request.POST and len(self.request.POST['reg_id']) > 0:
      reg_id_set.add(self.request.POST['reg_id'])
      self.response.set_status(200)
      return
    self.response.set_status(400)


class UnregisterHandler(webapp2.RequestHandler):
  """
    Stores the registration ids sent via the 'reg_id' parameter

    Sample request:
    curl http://localhost:8080/unregister?reg_id=test_id
  """
  def get(self):
    global reg_id_set
    if 'reg_id' in self.request.GET and len(self.request.GET['reg_id']) > 0:
      reg_id_set.remove(self.request.GET['reg_id'])
      self.response.set_status(200)
      return
    self.response.set_status(400)

  def post(self):
    """
      Stores the registration ids sent via the 'reg_id' parameter

      Sample request:
      curl -d "reg_id=test_id" http://localhost:8080/unregister
    """
    global reg_id_set
    if 'reg_id' in self.request.POST and len(self.request.POST['reg_id']) > 0:
      reg_id_set.remove(self.request.POST['reg_id'])
      self.response.set_status(200)
      return
    self.response.set_status(400)


class SendHandler(webapp2.RequestHandler):
  """
    Sends a message to the devices.
    The message is specified by the 'msg' parameter.
    The devices are specified by the 'reg_id' parameter. If
    the request does
    not contain any registration ids, the message will
    be sent to all
    devices recorded by /register

    Sample request:
    curl -d "reg_id=test_id&msg=Hello" http://localhost:8080/send
  """
  def post(self):
    from api_key import API_KEY
    global reg_id_set

    msg = self.request.get('msg', default_value='Greetings from the cloud!')
    reg_id_list = None
    if 'reg_id' in self.request.POST and len(self.request.POST['reg_id']) > 0:
      reg_id_list = [self.request.POST['reg_id']]

    if reg_id_list is None:
      sys.stderr.write('Sending message to all registered devices\n')
      reg_id_list = list(reg_id_set)

    data = {
      'registration_ids' : reg_id_list,
      'data' : {
        'msg' : msg
      }
    }

    headers = {
      'Content-Type' : 'application/json',
      'Authorization' : 'key=' + API_KEY
    }

    url = 'https://android.googleapis.com/gcm/send'
    request = urllib2.Request(url, json.dumps(data), headers)

    try:
      response = urllib2.urlopen(request)
      self.response.set_status(200)
      self.response.headers['Content-type'] = 'text/html'
      self.response.out.write(self.make_gcm_summary(data, response))
      return
    except urllib2.HTTPError, e:
      print e.code
      print e.read()

    return

  def make_gcm_summary(self, data, response):
    """
      Helper function to display the result of a /send request.
    """
    json_string = response.read()
    json_response = json.loads(json_string)

    html = """
<html>
  <head>
    <title>GCM send result</title>
  </head>
  <body>
    <h2>Request</h2>
    <pre>%s</pre>
    <h2>Response</h2>
    <pre>%s</pre>
    <h3>Per device</h3>
    <ol>""" % (repr(data), json_string)

    reg_id_list = data['registration_ids']
    for i in xrange(len(reg_id_list)):
      reg_id = reg_id_list[i]
      result = json_response['results'][i]

      html += """
        <li>
          reg_id: <code>%s</code><br/>
          <pre>%s</pre>
        </li>""" % (reg_id, json.dumps(result))

      html += """
    </ol>
    <a href="/">Back</a>
  </body>
</html>"""
    return html


app = webapp2.WSGIApplication([('/', Main),
                               ('/send', SendHandler),
                               ('/register', RegisterHandler),
                               ('/unregister', UnregisterHandler)], debug=True)
