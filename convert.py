import json
from geopy import distance
import numpy
import math



with open('migros_data.json') as json_file:
    data = json.load(json_file)
    i = 0
    migros = []
    while i < len(data["stores"]):
        name = data["stores"][i]["name"]
        mig_lon = data["stores"][i]["location"]["geo"]["lon"]
        mig_lat = data["stores"][i]["location"]["geo"]["lat"]
        type = data["stores"][i]["type"]
        cur_dict = {
            "name":  name ,
            "lat": mig_lat,
            "lon": mig_lon,
            "type": type
        }
        migros.append(cur_dict)
        i += 1

with open("migros_data_conv.json", "w") as outfile:
    s = json.dumps(migros)
    outfile.write(s)
