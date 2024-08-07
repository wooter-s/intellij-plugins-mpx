variable "used_variable" {
  description = "This variable is used in resource configuration"
  type        = string
  default     = "ami-01456a894f71116f2"
}

locals {
  instance_name = "used-instance"
}

resource "aws_instance" "example1" {
  ami           = var.used_variable
  instance_type = "t2.micro"

  tags = {
    Name = local.instance_name
  }
}

variable "unused_variable1" {
  description = "This variable is not used anywhere in the configuration"
  type        = string
  default     = "default_value1"
}

variable "unused_variable2" {
  description = "Another unused variable"
  type        = bool
  default     = true
}

locals {
  unused_local1 = "This is an unused local value"
  unused_local2 = 42
}

resource "aws_instance" "example2" {
  ami           = "ami-0123456789abcdef0"
  instance_type = "t2.small"

  tags = {
    Name = "another-example-instance"
  }
}

variable "partially_used_variable" {
  description = "This variable is used partially in the configuration"
  type        = string
  default     = "partially_used_value"
}

locals {
  partially_used_local = "partially used local value"
}

resource "aws_instance" "example3" {
  ami           = var.partially_used_variable
  instance_type = "t2.medium"

  tags = {
    Name = "partial-instance"
  }
}

output "partial_local_output" {
  value = local.partially_used_local
}
