object H {

    class Wizard {

    }

    def main(args: Array[String]): Unit = {
        println("Welcome to Wizard!")


        def bar(cellWidth: Int = 4, cellNum: Int = 2) =
            (("-" * cellWidth) * cellNum) + "-" + "\n"

        def bar2(cellWidth: Int = 14, cellNum: Int = 2) =
            (("-" * cellWidth) * cellNum) + "-" + "\n"

        def cells(cellWidth: Int = 7, cellNum: Int = 1) =
            ("|" + " " * cellWidth) * cellNum + "|" + "\n"

        def cells2(cellWidth: Int = 6, cellNum: Int = 1) =
            "|" + " game  " + "|" + "\n"

        def cells3(cellWidth: Int = 6, cellNum: Int = 1) =
            "|" + " trump " + "|" + "\n"

        def cells4(cellWidth: Int = 6, cellNum: Int = 1) =
            ("|" + "Set win" + "|" + " ")*3 + "\n"

        def cells5(cellWidth: Int = 7, cellNum: Int = 1) =
            (("|" + " " * cellWidth) * cellNum + "|" + " ")*3 + "\n"


        //def mesh =
        //    (bar() + cells() * 3) + bar()

        def mesh2: String =
            bar() + cells() + cells2() + cells() + bar()

        def mesh3: String =
            bar() + cells() + cells3() + cells() + bar()

        def mesh4: String =
            bar2() + cells5() + cells4() + cells5() + bar2()

        println(mesh2)
        println(mesh3)
        println(mesh4)

    }
}