import pandas as pd
low = .0
high = .95

def save(path):
    data = pd.read_csv(path)
    quantiles = data.quantile([low, high])
    filtered = data.apply(lambda x: x[(x>quantiles.loc[low,x.name]) & (x<quantiles.loc[high,x.name])], axis=0)

    print (path)
    print ("SD: " + str(data.values.std()))
    print ("Mean: " + str(data.values.mean()))
    print (" ")

    filtered.plot.hist(bins=40).figure.savefig(path + "_figure.png")

save("state.txt")
save("lookup.txt")
save("set.txt")


