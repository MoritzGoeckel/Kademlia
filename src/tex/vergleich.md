### Comparison

- Chord provides better scalability in terms of messaging cost than Kedemlia (many keep alive messages in Kademlia) [2]
- Kademlia has around 14% smaller messages than Chord (because of many simple keep alive messages) [2]
- Kademlia has around 40% shorter lookup in path length compared to Chord (routing information is more up to date and topology is used better) [2]
- Kademlias iterative routing is slightly slower than chords recursive routing [1] 
- Chord uses less bandwith than Kademlia when bandwith is scarse (can prioritize to stabilize successors over finger tables) [1]
- Tapestry, Kademlia, Chord and Kelips all achieve similar performance under churn, as long as the parameters are sufficiently tuned [1]
- Kademlia is faster in lookup, Chord scales better with a huge amount of nodes [2]
- Pastry incorporates information about the network such as ping time [3]
- Chord, Pastry and Tapestry have no build replication or hotspot avoidance [5]
- In Pastry most lookups fail when there is excessive churn [5]
- CAN has constant routing state and leave/join overhead but route length rises faster than log(n) [5]
- CAN performs better than Plaxton-based schemes under excessive churn [5]
- Pastry is less complex than Tapestry [6]
- Kademlia, Chord, Pastry and Tapestry have the same time complexity [4]
- Most known DHTs are Chord, CAN, Pastry, Tapestry, Kademlia

### Complexity [4]

![alt text](dhtperformance.png)

### Conclusion

- I would like to implement one of the more known DHTs. Therefore it is either Chord, CAN, Pastry, Tapastry or Kademlia
- Not CAN, as it scales not that well
- Not Tapestry, as it is a lot more complex than Pastry
- Not Pastry, as Im not interested in network topology for the project and Pastry does not perform that well under excessive churn
- Kademlia, because its the most efficient in lookup hops and message size
- Chord, because it looks the simplest

**So its either Kademlia for its efficiency or Chord for its simplicity**

### Sources

- [1] Jinyang Li, Jeremy Stribling, Thomer M. Gil, Robert Morris, M. Frans Kaashoek (2004): Comparing the performance of distributed hash tables under churn
- [2] Erkki Harjula, Timo Koskela, Mika Ylianttila (2011): Comparing the Performance and Efficiency of Two Popular DHTs in Interpersonal Communication
- [3] Syed Jafar Naqvi (2017): Peer to peer protocols explained - https://medium.com/karachain/peer-to-peer-protocols-explained-3b1d947c4600
- [4] Telesphore Tiendrebeogo, Daouda Ahmat, and Damien Magon (2012): Reliable and Scalable Distributed Hash Tables Harnessing Hyperbolic Coordinates
- [5] Eng Keong Lua, Marcleo Pias, Ravi Sharma, Steven Lim (2005): A survey and comparison of peer-to-peer overlay network schemes
- [6] Antony Rowstron, Peter Druschel (2001): Pastry: Scalable, decentralized object location and routing for large-scale peer-to-peer systems
