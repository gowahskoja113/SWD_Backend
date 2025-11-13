-- Schema customizations for H2
-- Ensure VIN is unique only when NOT NULL to avoid duplicates across NULLs
CREATE UNIQUE INDEX IF NOT EXISTS uk_vehicle_unit_vin_notnull
  ON vehicle_unit(vin) WHERE vin IS NOT NULL;

