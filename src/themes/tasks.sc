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
                if ($parseTree.date && $parseTree.date.value) $session.newTask.deadline = $parseTree.date.value;
                $session.newTask.status = "TO DO";
                $session.newTask.createdAt = moment();
                $session.newTask.id = $client.tasks.length;
                $client.tasks.push($session.newTask);
            a: Задача создана, её ID: {{ $session.newTask.id}}
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

        state: Search
            q: * (@New/@Hot/@Status) *
            script:
                if ($session.buttonsPaginationMessage) deleteMessage($session.buttonsPaginationMessage);
                $session.buttonsPaginationMessage = sendMessage("Выберите задачу: введите её id или нажмите на соответствующую кнопку",
                    pagination(_.map($client.tasks, function(task) {
                        return {text: task.name + " (" + task.id + ")"};
                    }), $session.paginatorCurPos, 5));
            
            state: GetNumber
                q: * @duckling.number::number *
                script:
                    log("0 " + toPrettyString($parseTree));
                    delete $session.buttonsPaginationMessage;
                    $session.task = _.find($client.tasks, function(task) {
                        return task.id == $parseTree._number
                    });
                    log("1 " + toPrettyString($session.task));
                go!: /Tasks/GetTasks/ShowTask
            
            state: MoreBack
                q: * (вперед:more) *
                q: * (назад:back) *
                q: * (вперед:more/назад:back) *
                script:
                    deleteMessage($session.buttonsPaginationMessage);
                    var mod = $parseTree.value === "more" ? 3 : -3;
                    $session.paginatorCurPos += mod;
                    $reactions.transition("/Tasks/GetTasks/Search");

        state: ShowTask
            script:
                $temp.body = "Название: " + $session.task.name + "\n";
                $temp.body += $session.task.description ? "Описание: " + $session.task.description + "\n" : "";
                $temp.body += $session.task.deadline ? "Дедлайн: " + moment($session.task.deadline).format("Do MMMM h:mm") + "\n" : "";
                $temp.body += "Статус: " + $session.task.status + "\n";
                $temp.body += "\n(Создано: " + moment($session.task.createdAt).format("Do MMMM h:mm") + ")";
            a: {{$temp.body}}
            buttons:
                "Обновить" -> /Tasks/UpdateTask
                "Удалить" -> /Tasks/DeleteTask
                "Вернуться к списку" -> /Tasks/GetTasks/Search
                "Вернуться в меню" -> /HowCanIHelpYou
            q: * {(обновить) [@Task]} * || toState = "/Tasks/UpdateTask"
            q: * {(удалить) [@Task]} * || toState = "/Tasks/DeleteTask"

        state: NoTasks
            a: На текущий момент у вас нет задач. Хотите создать?
            buttons:
                "Да, создать задачу" -> /Tasks/CreateTask
                "Нет, вернуться в меню" -> /HowCanIHelpYou

    state: UpdateTask
        a: Что именно вы хотите изменить?
        buttons:
            "Название"
            "Описание"
            "Дедлайн"
            "Статус"
            "Вернуться к списку" -> /Tasks/GetTasks/Search

        state: UpdateField
            q: * (@Name/@Description/@Deadline/@Status) *
            a: Поле обновлено

    state: DeleteTask
        a: Вы точно хотите удалить задачу?
        buttons:
            "Да" -> /Tasks/DeleteTask/Confirm
            "Нет" -> /Tasks/DeleteTask/Cancel
            "Вернуться к списку" -> /Tasks/GetTasks/Search

        state: Confirm
            q: подтверждаю
            script: $client.tasks.remove($session.task.id);
            a: Хорошо! Задача {{$session.task.id}} удалена

        state: Cancel
            q: отмена
            a: Хорошо, возвращаю тебя к задаче
            go!: /Tasks/GetTasks