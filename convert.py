import json
from geopy import distance
import numpy
import math



with open('migros_data.json') as json_file:
    data = json.load(json_file)
    i = 0
    migros = {}
    while i < len(data["stores"]):
        name = data["stores"][i]["name"]
        mig_lon = data["stores"][i]["location"]["geo"]["lon"]
        mig_lat = data["stores"][i]["location"]["geo"]["lat"]
        type = data["stores"][i]["type"]
        string = "migros"+ str(i) + ' = {"name": "' + name + '","lat":'+ str(mig_lat) + ',"lon":' + str(mig_lon) + ',"type":"' + str(type) +'"}'
        exec(str(string))
        #exec('print(' + "migros"+ str(i)+")" )
        migrosstring = "asdf"
        exec('migrosstring = json.dumps(' + "migros"+ str(i)+")" )
        migros[i]= migrosstring
        i += 1

with open("migros_data_conv.json", "w") as outfile:
    s = json.dumps(migros)
    outfile.write(s)
