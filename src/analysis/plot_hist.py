import pandas as pd
import matplotlib.pyplot as plt

low = .0
high = .95

def save(path, doFilter, statsfile, num):
    print ("Plotting " + path)

    data = pd.read_csv(path)

    if doFilter:
        quantiles = data.quantile([low, high])
        filtered = data.apply(lambda x: x[(x>quantiles.loc[low,x.name]) & (x<quantiles.loc[high,x.name])], axis=0)

    print (path)
    print ("SD: " + str(data.values.std()))
    print ("Mean: " + str(data.values.mean()))
    print (" ")

    f = open(statsfile, "a+")
    f.write(num + "\t" + str(data.values.mean()) + "\t"+ str(data.values.std()) + "\t" + str(data.values.min()) + "\t" + str(data.values.max()) + "\r\n")
    f.close()

    if doFilter:
        filtered.plot.hist(bins=40).figure.savefig(path + "_figure.png")
    else:
        data.plot.hist(bins=40).figure.savefig(path + "_figure.png")

def writeHead(statsfile):
    f = open(statsfile, "a+")
    f.write("Nodes\tMean\tSD\tMin\tMax\r\n")
    f.close()

def saveAll(num):
    save("state_"+str(num)+".txt", False, "state", str(num))
    save("lookup_"+str(num)+".txt", False, "lookup", str(num))
    save("set_"+str(num)+".txt", False, "set", str(num))

def plotStats(filename, title):
    data = pd.read_csv(filename,delimiter="\t")
    x = data["Nodes"]
    y = data["Mean"]
    std = data["SD"]

    fig = plt.figure(figsize=(10, 8))
    fig.suptitle(title, fontsize="large")
    fig.subplots_adjust(hspace=0.4)    

    ax1 = fig.add_subplot(3,1,1)
    ax1.plot(x, y, marker="o")
    ax1.set_xscale('log')
    #ax1.set_xlabel('Nodes in the network')
    ax1.set_ylabel('Mean')

    ax2 = fig.add_subplot(3,1,2)
    ax2.plot(x, std, marker="o")
    ax2.set_xscale('log')
    #ax2.set_xlabel('Nodes in the network')
    ax2.set_ylabel('Std')

    ax3 = fig.add_subplot(3,1,3)
    ax3.plot(x, data["Min"], marker="o", label="Min")
    ax3.plot(x, data["Max"], marker="o", label="Max")
    ax3.set_xscale('log')
    ax3.set_xlabel('Nodes in the network')
    ax3.set_ylabel('Min/Max')

    fig.savefig(filename + "_mean_figure.png")

    #ax.errorbar(x, y, yerr=std, fmt='-o')    
    # data.set_index('Nodes')
    # data.plot().figure.savefig(filename + "_figure.png")

writeHead("state")
writeHead("lookup")
writeHead("set")

saveAll(10)
saveAll(100)
saveAll(500)
saveAll(1000)
saveAll(5000)
saveAll(10000)
saveAll(100000)

plotStats("state", "State/Node")
plotStats("lookup", "Hops/Lookup")
plotStats("set","Hops/Store")
