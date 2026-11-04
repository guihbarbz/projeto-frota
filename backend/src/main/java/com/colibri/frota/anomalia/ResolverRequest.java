package com.colibri.frota.anomalia;

import jakarta.validation.constraints.Size;

public record ResolverRequest(@Size(max = 500) String nota) {
}