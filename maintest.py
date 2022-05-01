import json
from geopy import distance
import numpy
import math

def get_bearing(lat1, long1, lat2, long2):
    dLon = (long2 - long1)
    x = math.cos(math.radians(lat2)) * math.sin(math.radians(dLon))
    y = math.cos(math.radians(lat1)) * math.sin(math.radians(lat2)) - math.sin(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.cos(math.radians(dLon))
    brng = numpy.arctan2(x,y)
    brng = numpy.degrees(brng)

    return brng


my_loc = "47.21098217245563, 8.268387814407712"
my_lat = 47.21098217245563
my_lon = 8.268387814407712
with open('migros_data.json') as json_file:
    data = json.load(json_file)
    i = 0
    while i <= len(data["stores"])/2:
        name = data["stores"][i]["name"]
        mig_lon = data["stores"][i]["location"]["geo"]["lon"]
        mig_lat = data["stores"][i]["location"]["geo"]["lat"]
        mig_loc = str(mig_lat)+","+str(mig_lon)
        dist = distance.distance(my_loc,mig_loc).km
        bearing = get_bearing(my_lat,my_lon,mig_lat,mig_lon)
        print(name)
        print(dist)
        print(bearing)
        i += 1