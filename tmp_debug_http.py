import urllib.request
import urllib.error
import urllib.parse

url = 'http://localhost:8080/register'
data = urllib.parse.urlencode({
    'name': 'Test User',
    'email': 'testuser@example.com',
    'password': 'secret123',
    'role': 'CANDIDATE'
}).encode('utf-8')
req = urllib.request.Request(url, data=data, method='POST')
req.add_header('Content-Type', 'application/x-www-form-urlencoded')
try:
    with urllib.request.urlopen(req) as r:
        print('STATUS', r.status)
        print(r.read(1000).decode('utf-8'))
except urllib.error.HTTPError as e:
    print('HTTPERROR', e.code)
    print(e.read().decode('utf-8'))
except Exception as e:
    print('ERROR', e)
