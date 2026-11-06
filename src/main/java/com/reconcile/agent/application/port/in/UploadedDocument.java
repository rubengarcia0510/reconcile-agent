package com.reconcile.agent.application.port.in;

public record UploadedDocument(String filename, byte[] content) {}
