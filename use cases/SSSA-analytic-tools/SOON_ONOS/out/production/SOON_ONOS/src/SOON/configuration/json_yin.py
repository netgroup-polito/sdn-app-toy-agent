import json, xmltodict

o = xmltodict.parse(open("yinFile.xml", "r").read())
open("yinFile.json", "w").write(json.dumps(o, indent=4))