# polsh
polsh is a sell and scripting language based on reverse-polish-notation, and developed in Scala

```
> echo "3 2 3 4 5 2 / + + - *" | scala polsh.scala 
List(3)
List(3, 2)
List(3, 2, 3)
List(3, 2, 3, 4)
List(3, 2, 3, 4, 5)
List(3, 2, 3, 4, 5, 2)
List(3, 2, 3, 4, 5, 2, /)
List(3, 2, 3, 4, 2)
List(3, 2, 3, 4, 2, +)
List(3, 2, 3, 6)
List(3, 2, 3, 6, +)
List(3, 2, 9)
List(3, 2, 9, -)
List(3, -7)
List(3, -7, *)
List(-21)
```
