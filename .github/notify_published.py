import requests
import simplejson as json
import re

def get_info():
	headers = {'User-Agent': 'PostmanRuntime/7.29.0', 'Accept': '*/*', 'Accept-Encoding': 'gzip, deflate, br', 'Connection': 'keep-alive'}
	r = requests.get('https://github.com/promcteam/promccore/packages/1203729', headers=headers)

	if r.status_code == 200:
		version = re.findall('&amp;lt;version&amp;gt;((.|\w|\n)*?)&amp;lt;\/version&amp;gt;', str(r.content))[0][0]
		id = re.findall('&amp;lt;artifactId&amp;gt;((.|\w|\n)*?)&amp;lt;\/artifactId&amp;gt;', str(r.content))[0][0]
		return version, id

version, name = get_info()
embed = {
	'author': {
		'name': 'New Build Available!',
		'url': 'https://github.com/promcteam/' + name
	},
	'title': name + '-' + version,
	'url': 'https://github.com/promcteam/promccore/packages/1203729',
	'color': 5341129
}

requests.post('https://discord.com/api/webhooks/986503790435926057/KCApc69ofUTBQC3dd9huu4T748duS5EZluhB4AWwMwIq1UiQew0XIKNI-eVGPHNUt6E9',
	headers={'Content-Type': 'application/json'},
	data=json.dumps({'embeds': [embed]})
)
