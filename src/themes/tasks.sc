theme: /Tasks
    
    state: CreateTask
        if: _.isEmpty($session.newTask)
            a: Напишите название задачи
        else:
            go!: /Tasks/CreateTask/ClarifyTask
        q: * || toState="/Tasks/CreateTask/GetName"

        state: ResetTask
            script: delete $session.newTask;
            go: /Tasks/CreateTask

        state: GetName
            if: !$session.newTask || _.isEmpty($session.newTask.description)
                script:
                    $session.newTask = $session.newTask || {};
                    $session.newTask.name = $session.newTask.name || $request.query;
                a: Напишите описание для задачи
            else:
                go!: /Tasks/CreateTask/GetDescription
            buttons:
                "Нет описания" -> /Tasks/CreateTask/GetDescription
            q: * || toState="/Tasks/CreateTask/GetDescription"

        state: GetDescription
            script: $session.newTask.description = $request.query;
            a: Укажите дедлайн выполнения задачи
            buttons:
                "Нет дедлайна" -> /Tasks/CreateTask/GetDeadline
            q: * || toState="/Tasks/CreateTask/GetWrongDeadline"
            q: * (нет/без/~бессрочный/~не ~важен) * || toState="/Tasks/CreateTask/GetDeadline"
            q: * @duckling.date::date * || toState="/Tasks/CreateTask/GetDeadline"

        state: GetDeadline
            a: Отлично! Приступаю к созданию задачи
            script:
                if ($parseTree._date) $session.newTask.deadline = moment($parseTree._date).add(3, "h").subtract(1, 'months');
                $session.newTask.status = $injector.statuses[0];
                $session.newTask.createdAt = moment().add(3, "h");
                $session.newTask.id = $client.tasks.length;
                log("!!! " + toPrettyString($parseTree));
                log("!!! " + toPrettyString($session.newTask));
                $client.tasks.push($session.newTask);
            a: Задача создана, её ID: {{$session.newTask.id}}
            script: delete $session.newTask;
            go!: /HowCanIHelpYou

        state: GetWrongDeadline
            a: К сожалению, не удалось распознать дату в вашем ответе. Попробуйте ещё раз
            go!: /Tasks/CreateTask/GetName

        state: ClarifyTask
            if: $session.newTask.description
                a: Вы уже заполняли заявку:
                    Название: {{$session.newTask.name}}
                    Описание: {{$session.newTask.description}}
                    Хотите продолжить заполнение?
            else:
                a: Вы уже заполняли заявку:
                    Название: {{$session.newTask.name}}
                    Хотите продолжить заполнение?
            buttons:
                "Да" -> /Tasks/CreateTask/GetName
                "Нет" -> /Tasks/CreateTask/ResetTask
                "Вернуться в меню" -> /HowCanIHelpYou

    state: GetTasks
        script:
            $session.paginatorCurPos = 0;
        if: _.isEmpty($client.tasks)
            go!: /Tasks/GetTasks/NoTasks
        else:
            a: В каком виде вы хотите посмотреть задачи?
            buttons:
                "Сначала новые"
                "Сначала горящие"
                "С определённым статусом"
        q: * @New * || toState = "/Tasks/GetTasks/ChooseNewTaskSearch"
        q: * @Hot * || toState = "/Tasks/GetTasks/ChooseHotTaskSearch"
        q: * @Status * || toState = "/Tasks/GetTasks/ChooseStatusTaskSearch"

        state: ChooseNewTaskSearch
            go!: /Tasks/Search

        state: ChooseHotTaskSearch
            go!: /Tasks/Search

        state: ChooseStatusTaskSearch
            a: Выберите статус
            script:
                var buttons = [];
                _.each($injector.statuses, function(status) {
                    buttons.push({text: status});
                });
                $reactions.buttons(buttons);
            
            state: Confirm
                q: *
                go!: /Tasks/Search

        state: NoTasks
            a: На текущий момент у вас нет задач. Хотите создать?
            buttons:
                "Да, создать задачу" -> /Tasks/CreateTask
                "Нет, вернуться в меню" -> /HowCanIHelpYou

    state: Search
        script:
            if ($session.buttonsPaginationMessage) deleteMessage($session.buttonsPaginationMessage);
            $session.buttonsPaginationMessage = sendMessage("Выберите задачу: введите её id или нажмите на соответствующую кнопку",
                pagination(_.map($client.tasks, function(task) {
                    return {text: task.name + " (" + task.id + ")"};
                }), $session.paginatorCurPos, $injector.tasksOnPage));
        
        state: GetNumber
            q: * @duckling.number::number *
            script:
                delete $session.buttonsPaginationMessage;
                $session.task = _.find($client.tasks, function(task) {
                    return task.id == $parseTree._number
                });
            go!: /Tasks/ShowTask
        
        state: MoreBack
            q: * (вперед:more) *
            q: * (назад:back) *
            q: * (вперед:more/назад:back) *
            script:
                deleteMessage($session.buttonsPaginationMessage);
                var mod = $injector.tasksOnPage;
                if ($parseTree.value !== "more") mod *= -1;
                var paginatorCurPos = $session.paginatorCurPos + ($parseTree.value !== "more") ? mod * -1 : mod;
                if (paginatorCurPos > 0) {
                    mod -= 1;
                    if (paginatorCurPos + 4 < $client.tasks.length) mod -= 1;
                } else {
                    if (paginatorCurPos + 5 < $client.tasks.length) mod -= 1;
                }
                $session.paginatorCurPos += ($parseTree.value !== "more") ? mod * -1 : mod;
                $reactions.transition("/Tasks/Search");

    state: ShowTask
        script:
            $temp.body = "Название: " + $session.task.name + "\n";
            $temp.body += $session.task.description ? "Описание: " + $session.task.description + "\n" : "";
            $temp.body += $session.task.deadline ? "Дедлайн: " + moment($session.task.deadline).locale("ru").format("Do MMMM") + "\n" : "";
            $temp.body += "Статус: " + $session.task.status + "\n";
            $temp.body += "\n(Создано: " + moment($session.task.createdAt).locale("ru").format("Do MMMM h:mm") + ")";
        a: {{$temp.body}}
        buttons:
            "Обновить" -> /Tasks/UpdateTask
            "Удалить" -> /Tasks/DeleteTask
            "Вернуться к списку" -> /Tasks/Search
            "Вернуться в меню" -> /HowCanIHelpYou
        q: * {(обновить) [@Task]} * || toState = "/Tasks/UpdateTask"
        q: * {(удалить) [@Task]} * || toState = "/Tasks/DeleteTask"

    state: UpdateTask
        a: Что именно вы хотите изменить?
        buttons:
            "Название"
            "Описание"
            "Дедлайн"
            "Статус"
            "Вернуться" -> /Tasks/ShowTask

        state: UpdateName
            q: * @Name *
            a: Введите новое название задачи
            
            state: Confirm
                q: *
                script: $session.task.name = $request.query;
                go!: /Tasks/ShowTask

        state: UpdateDescription
            q: * @Description *
            a: Введите новое описание задачи
            
            state: Confirm
                q: *
                script: $session.task.description = $request.query;
                go!: /Tasks/ShowTask

        state: UpdateDeadline
            q: * @Deadline *
            a: Введите новый дедлайн
            
            state: Confirm
                q: * @duckling.date::date *
                script: $session.task.deadline = moment($parseTree._date).add(3, "h").subtract(1, 'months');
                go!: /Tasks/ShowTask

            state: WrongDeadline
                q: *
                a: К сожалению, не удалось распознать дату в вашем ответе. Попробуйте ещё раз
                go: /Tasks/UpdateTask/GetDeadline

        state: UpdateStatus
            q: * @Status *
            a: Выберите статус
            script:
                var buttons = [];
                _.each($injector.statuses, function(status) {
                    buttons.push({text: status});
                });
                $reactions.buttons(buttons);
            
            state: Confirm
                q: *
                script: $session.task.status = $request.query;
                go!: /Tasks/ShowTask

    state: DeleteTask
        a: Вы точно хотите удалить задачу?
        buttons:
            "Да" -> /Tasks/DeleteTask/Confirm
            "Нет" -> /Tasks/DeleteTask/Cancel
            "Вернуться к списку" -> /Tasks/Search

        state: Confirm
            q: подтверждаю
            script:
                $client.tasks = _.filter($client.tasks, function(task) {
                    return $session.task.id != task.id;
                });
            a: Хорошо! Задача {{$session.task.id}} удалена
            go!: /Tasks/GetTasks

        state: Cancel
            q: отмена
            a: Хорошо, возвращаю тебя к задаче
            go!: /Tasks/ShowTask