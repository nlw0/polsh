# polsh
polsh is a shell and scripting language based on reverse polish notation, and developed in Scala. Just a toy project, not sure it is going anywhere.

```
> echo "13 x store x load 1 x load - * x store" | scala polsh.scala 
List(13) Map()
List(13, x) Map()
List(13, x, store) Map()
List(x) Map(x -> 13)
List(x, load) Map(x -> 13)
List(13) Map(x -> 13)
List(13, 1) Map(x -> 13)
List(13, 1, x) Map(x -> 13)
List(13, 1, x, load) Map(x -> 13)
List(13, 1, 13) Map(x -> 13)
List(13, 1, 13, -) Map(x -> 13)
List(13, -12) Map(x -> 13)
List(13, -12, *) Map(x -> 13)
List(-156) Map(x -> 13)
List(-156, x) Map(x -> 13)
List(-156, x, store) Map(x -> 13)
PolshCpu(Stream(),Map(x -> -156))
```
