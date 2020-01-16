# http://stackoverflow.com/questions/5725430/http-test-server-that-accepts-get-post-calls
# import urllib

import urllib.request
import urllib.parse

url = 'http://httpbin.org/post'
appid = 'Echo HTTP POST Demo'

context = '''
Italian sculptors and painters of the renaissance favored
the Virgin Mary for inspiration
'''
query = 'madonna'

params = urllib.parse.urlencode({
	'appid': appid,
	'context': context,
	'query': query
})

params = params.encode('ascii')

# data = urllib.request.urlopen(url, params).read()
# print(data)

with urllib.request.urlopen(url, params) as f:
    print(f.read().decode('utf-8'))


