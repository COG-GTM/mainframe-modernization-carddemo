class MockSendReceive {
    constructor() {
        this.sentMaps = [];
        this.receivedMaps = [];
        this.currentMapData = {};
        this.shouldFailSend = false;
        this.shouldFailReceive = false;
        this.responseCode = 'NORMAL';
    }

    reset() {
        this.sentMaps = [];
        this.receivedMaps = [];
        this.currentMapData = {};
        this.shouldFailSend = false;
        this.shouldFailReceive = false;
        this.responseCode = 'NORMAL';
    }

    setFailure(operation, shouldFail, responseCode = 'MAPFAIL') {
        if (operation === 'SEND') {
            this.shouldFailSend = shouldFail;
        } else if (operation === 'RECEIVE') {
            this.shouldFailReceive = shouldFail;
        }
        this.responseCode = responseCode;
    }

    send(mapName, mapset, mapData, options = {}) {
        if (this.shouldFailSend) {
            throw new Error(`SEND failed: ${this.responseCode}`);
        }

        const sentMap = {
            mapName,
            mapset,
            mapData: JSON.parse(JSON.stringify(mapData)),
            options,
            timestamp: new Date()
        };

        this.sentMaps.push(sentMap);
        this.currentMapData = mapData;

        return {
            responseCode: 'NORMAL',
            mapName,
            mapset
        };
    }

    receive(mapName, mapset, inputData = {}) {
        if (this.shouldFailReceive) {
            throw new Error(`RECEIVE failed: ${this.responseCode}`);
        }

        const receivedMap = {
            mapName,
            mapset,
            inputData: JSON.parse(JSON.stringify(inputData)),
            timestamp: new Date()
        };

        this.receivedMaps.push(receivedMap);

        return {
            responseCode: 'NORMAL',
            mapName,
            mapset,
            data: inputData
        };
    }

    getLastSentMap() {
        return this.sentMaps[this.sentMaps.length - 1] || null;
    }

    getLastReceivedMap() {
        return this.receivedMaps[this.receivedMaps.length - 1] || null;
    }

    getSentMapHistory() {
        return [...this.sentMaps];
    }

    getReceivedMapHistory() {
        return [...this.receivedMaps];
    }

    wasMapSent(mapName) {
        return this.sentMaps.some(map => map.mapName === mapName);
    }

    wasMapReceived(mapName) {
        return this.receivedMaps.some(map => map.mapName === mapName);
    }

    getCurrentMapData() {
        return JSON.parse(JSON.stringify(this.currentMapData));
    }

    setMapFieldValue(fieldName, value) {
        this.currentMapData[fieldName] = value;
    }

    getMapFieldValue(fieldName) {
        return this.currentMapData[fieldName];
    }

    validateMapStructure(mapData, expectedFields) {
        const missingFields = expectedFields.filter(field => !(field in mapData));
        if (missingFields.length > 0) {
            throw new Error(`Missing required map fields: ${missingFields.join(', ')}`);
        }
        return true;
    }
}

module.exports = MockSendReceive;
