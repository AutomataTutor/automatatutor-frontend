package com.automatatutor.model

import net.liftweb.mapper._

class Role extends LongKeyedMapper[Role] with IdPK {
	def getSingleton = Role

	object roleId extends MappedLong(this)
	object userId extends MappedLongForeignKey(this, User)
	
	def isStudentRole() = this.roleId.is == 1
	def isInstructorRole() = this.roleId.is == 2
	def isAdminRole() = this.roleId.is == 3
	
	override def toString() =
        this.roleId.is match {
          case 1 => "Student"
          case 2 => "Instructor"
          case 3 => "Admin"
        }
}

object Role extends Role with LongKeyedMetaMapper[Role] {
	def createStudentRoleFor(user : User) : Boolean = this.create.roleId(1).userId(user).save
	def createInstructorRoleFor(user : User) : Boolean = this.create.roleId(2).userId(user).save
	def createAdminRoleFor(user : User) : Boolean = this.create.roleId(3).userId(user).save
}
