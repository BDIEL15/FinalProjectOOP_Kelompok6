for row in range(6):
    for column in range(6):
        while not can_harvest():
            wait()
        harvest()

        if column < 5:
            if row % 2 == 0:
                move("east")
            else:
                move("west")

    if row < 5:
        move("north")
