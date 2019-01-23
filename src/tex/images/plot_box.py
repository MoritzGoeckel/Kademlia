import pandas as pd
import matplotlib.pyplot as plt

def plotStats(filename, title, minimum, maximum):
    data = pd.read_csv(filename,delimiter="\t")
    nodes = data["Nodes"]
    means = data["Mean"]
    std = data["SD"]
    mins = data["Min"]
    maxes = data["Max"]

    print(data);

    fig = plt.figure(figsize=(10, 8))
    fig.suptitle(title, fontsize="large")

    ax1 = fig.add_subplot(1,1,1)

    ax1.errorbar(nodes, means, std, fmt='ok', lw=3)
    ax1.errorbar(nodes, means, [means - mins, maxes - means],
             fmt='.k', ecolor='gray', lw=1, capsize=6)

    ax1.set_xscale('log')
    ax1.set_xlabel('Nodes in the network')
    ax1.set_ylabel('Min, Max, Mean, Std')

    #Left & right limit
    ax1.set_ylim(minimum, maximum)

    fig.savefig(filename + "_figure.png")
    #plt.show()

plotStats("state", "State/Node", 0, 1000)
plotStats("lookup", "Hops/Lookup", 0, 30)
plotStats("set","Hops/Store", 12, 30)
