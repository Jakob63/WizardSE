val eol = sys.props("line.seperator")
def bar(cellWidth: Int = 3, cellNum: Int = 3) = ("+" + "-" * cellWidth)* cellNum + "+" + eol
def cells(cellWidth: Int = 3, cellNum: Int = 3) = ("|" + " " * cellWidth) * cellNum + "|" + eol
def mesh(cellWidth: Int = 3, cellNum: Int = 3) = (bar(cellWidth, cellNum) + cells(cellWidth, cellNum)) * cellNum + bar(cellWidth, cellNum)


val eol = sys.props("line.seperator")
val bar = "+---+---+---+" + eol
val cells = "|   |   |   |" + eol
val mesh = (bar + cells) * 3 + bar

println(mesh)