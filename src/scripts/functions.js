$global.MODE = $injector.mode;

bind("postProcess", function() {
    $.session.lastState = $.currentState;
    return $.session.lastState;
});

function countRepeatsInRow() {
    $.temp.entryState = $.currentState;
    if ($.session.lastState === $.currentState) {
        $.session.repeatsInRow += 1;
    } else {
       $.session.repeatsInRow = 1;
    }
    return $.session.repeatsInRow;
}

function cancelEvent(eventId) {
        try {
            if (eventId) $pushgate.cancelEvent(eventId);
        } catch (err) {
            log(err);
        }
    }

function createScheduledEvent(interval, eventName, eventData, channelType, botId, chatId) {
    var whatTimeNow = $jsapi.dateForZone("Europe/Moscow", "yyyy-MM-dd HH:mm:ss");
    var scheduledTime = (moment(whatTimeNow).add(interval)).format("YYYY-MM-DDTHH:mm:ss");
    var event = $pushgate.createEvent(
        scheduledTime,
        eventName,
        eventData,
        channelType,
        botId,
        chatId
    );
    return event.id;
}

function getClientUsername() {
    if ($.request.channelType === "chatapi") return $.request.rawRequest.clientId;
    return "test";
}
